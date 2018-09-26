package auto;

import auto.bean.Field;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 根据数据库表字段自动生成 实体+Mybatis配置
 *
 * @author chensheng
 */
public class Entity {

    private static Configuration configuration = null;

    private final String ENTITY = "entity";

    private final String MAPPER_CLASS = "dao";

    private final String SERVICE = "service";

    private final String SRC = "/src/";

    private final String CONFIG = "/resource/mapper/";

    private final Boolean MYSQL = true;

    /**
     * 主键属性
     */
    Field primaryKeyField;

    /**
     * 其他属性
     */
    ArrayList<Field> fields = new ArrayList<Field>();

    /**
     * 文件夹路径
     */
    private String fileDir;

    /**
     * 代码内部导入包路径
     */
    private String entityPackage;

    private String mapperClassPackage;

    private String servicePackage;

    /**
     * 实体名称
     */
    private String entityName = null;

    private Connection conn;

    private String tableName;

    private String tableComment;

    public Entity(String fileDir, String basePakage) {
        this.fileDir = fileDir;
        this.entityPackage = basePakage + "." + ENTITY;
        this.mapperClassPackage = basePakage + "." + MAPPER_CLASS;
        this.servicePackage = basePakage + "." + SERVICE;
    }

    public void init() {
        if (configuration == null) {
            configuration = initFreemarkerConfiguration();
        }
    }

    /**
     * 初始化freemarker模板
     */
    private Configuration initFreemarkerConfiguration() {
        Configuration cfg = null;
        try {
            cfg = new Configuration(Configuration.VERSION_2_3_23);
            URL url = Thread.currentThread().getContextClassLoader().getResource("ftl");
            String path = url.getPath();
            cfg.setDirectoryForTemplateLoading(new File(path));
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.IGNORE_HANDLER);
        } catch (Exception e) {
            throw new RuntimeException("Freemarker 模板环境初始化异常!", e);
        }
        return cfg;
    }

    /**
     * 生成模板
     *
     * @param file 模板文件
     * @param data 模板数据
     */
    private String ftl(String file, Map data) {
        try {
            Writer out = new StringWriter(2048);
            configuration.getTemplate(file).process(data, out);
            return out.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 读取数据库配置文件 & 加载数据库连接
     */
    private void connectDB() throws ClassNotFoundException, SQLException {
        Class.forName(AutoProperties.jdbcdriver);
        conn = DriverManager.getConnection(AutoProperties.jdbcurl, AutoProperties.username, AutoProperties.password);
    }

    void initField(String tableName, String entityName, boolean deleteFlag)
            throws Exception {
        // 开始生成
        connectDB();

        this.tableName = tableName;
        this.entityName = entityName;

        Statement stmt = conn.createStatement();
        ResultSet result = stmt.executeQuery("select * from " + tableName + " where 1 = 2 ");
        ResultSetMetaData metData = result.getMetaData();
        int ColumnCount = metData.getColumnCount();
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet priKeySet = databaseMetaData.getPrimaryKeys(null, null, tableName);
        ResultSet columnSet = databaseMetaData.getColumns(null, null, this.tableName, null);

        ResultSet tableSet = null;
        if (MYSQL) {
            tableSet = stmt.executeQuery("select * from information_schema.TABLES where table_name ='" + tableName + "'");
        }

        String primaryKeyFieldName = null;
        String primaryKeyPropertyName = null;

        if (priKeySet.next()) {
            primaryKeyFieldName = priKeySet.getString("COLUMN_NAME");
            primaryKeyPropertyName = Convert.getJavaBeanPropsNameBy(primaryKeyFieldName);
            primaryKeyField = new Field(primaryKeyFieldName, primaryKeyPropertyName, true);
        }
        priKeySet.close();

        for (int i = 1; i <= ColumnCount; i++) {
            String fieldName = metData.getColumnName(i);
            int iColumnType = metData.getColumnType(i);

            if (!fieldName.equalsIgnoreCase(primaryKeyFieldName)) {
                String propertyName = Convert.getJavaBeanPropsNameBy(fieldName);
                Field field = new Field(fieldName, propertyName, false);
                field.setDataType(iColumnType);
                fields.add(field);
            } else {
                if (null != primaryKeyField) {
                    primaryKeyField.setDataType(iColumnType);
                    primaryKeyField.setAutoIncrement(metData.isAutoIncrement(i));
                }
            }
        }

        // 获取字段注解
        while (columnSet.next()) {
            String comment = columnSet.getString("REMARKS");
            String column = columnSet.getString("COLUMN_NAME");

            if (StringUtils.isNotBlank(comment)) {
                for (Field field : fields) {
                    if (StringUtils.equals(column, field.getFieldName())) {
                        field.setComment(comment);
                        break;
                    }
                }
            }
        }
        columnSet.close();

        // 表注解
        if (tableSet != null && tableSet.next()) {
            tableComment = tableSet.getString("TABLE_COMMENT");
        }
        tableSet.close();
        result.close();
        stmt.close();
        conn.close();
    }

    void buildEntity() {
        List<Field> fieldList = new ArrayList();
        fieldList.add(primaryKeyField);
        fieldList.addAll(fields);

        Map<String, Object> data = new HashMap(16);
        data.put("entityPackage", entityPackage);
        data.put("tableComment", tableComment);
        data.put("entityName", entityName);
        data.put("fieldList", fieldList);

        String codeBuffer = ftl("entity.ftl", data);

        // 生成文件
        String fileName = this.fileDir + SRC
                + entityPackage.replaceAll("\\.", "\\/") + "/" + this.entityName
                + ".java";

        System.out.println(fileName);

        File file = new File(fileName);

        try {
            FileUtils.writeStringToFile(file, codeBuffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void buildMapper() {
        String mapper = this.mapperClassPackage + "." + this.entityName + "Mapper";
        String entity = this.entityPackage + "." + this.entityName;
        Map<String, Object> data = new HashMap(16);
        data.put("mapper", mapper);
        data.put("entity", entity);
        data.put("tableName", tableName);
        data.put("pkField", primaryKeyField);
        data.put("fieldList", fields);
        List<Field> queryFieldList = new ArrayList<>();
        for (Field f : fields) {
            String dataType = f.getJavaType();

            //包含字段类型
            boolean include = StringUtils.equals(dataType, "String") || StringUtils.equals(dataType, "Integer") || StringUtils.equals(dataType, "Long") || StringUtils.equals(dataType, "Double") || StringUtils.equals(dataType, "Boolean");
            if (include) {
                queryFieldList.add(f);
            }
        }
        data.put("queryFieldList", queryFieldList);
        String codeBuffer = ftl("mapper.xml.ftl", data);

        // 生成文件
        String fileName = this.fileDir + CONFIG + this.entityName
                + "Mapper.xml";

        File file = new File(fileName);

        System.out.println(fileName);

        try {
            FileUtils.writeStringToFile(file, codeBuffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildMapperClass() {
        Map<String, Object> data = new HashMap(16);
        data.put("mapperClassPackage", mapperClassPackage);
        data.put("entityPackage", entityPackage);
        data.put("tableComment", tableComment);
        data.put("entityName", entityName);
        data.put("idJavaType", Convert.getDataType(this.primaryKeyField.getDataType()));
        String codeBuffer = ftl("mapper.ftl", data);

        // 生成文件
        String fileName = this.fileDir + SRC
                + mapperClassPackage.replaceAll("\\.", "\\/") + "/"
                + this.entityName + "Mapper.java";
        File file = new File(fileName);
        System.out.println(fileName);
        try {
            FileUtils.writeStringToFile(file, codeBuffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildService() {
        Map<String, Object> data = new HashMap(16);
        data.put("servicePackage", servicePackage);
        data.put("entityPackage", entityPackage);
        data.put("mapperClassPackage", mapperClassPackage);
        data.put("tableComment", tableComment);
        data.put("entityName", entityName);
        data.put("idJavaType", Convert.getDataType(this.primaryKeyField.getDataType()));
        String codeBuffer = ftl("service.ftl", data);

        // 生成文件
        String fileName = this.fileDir + SRC
                + servicePackage.replaceAll("\\.", "\\/") + "/" + this.entityName
                + "Service.java";
        File file = new File(fileName);

        System.out.println(fileName);

        try {
            FileUtils.writeStringToFile(file, codeBuffer, "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
