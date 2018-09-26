package auto.bean;

import auto.Convert;

/**
 * 字段属性
 *
 * @author chensheng
 */
public class Field {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 属性名
     */
    private String propertyName;

    /**
     * 字段类型
     */
    private int dataType;

    /**
     * 注解
     */
    private String comment;

    /**
     * JAva类型
     */
    private String javaType;

    /**
     * JAva类型
     */
    private String fullJavaType;

    /**
     * JDBC类型
     */
    private String jdbcType;

    private boolean primaryKey;
    private boolean autoIncrement = false;

    public Field(String fieldName, String propertyName, boolean primaryKey) {
        super();
        this.fieldName = fieldName;
        this.primaryKey = primaryKey;
        this.propertyName = propertyName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
        this.javaType = Convert.getDataType(dataType);
        this.jdbcType = Convert.getJDBCType(dataType);
        this.fullJavaType = Convert.getFullDataType(dataType);
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public String getJavaType() {
        return javaType;
    }

    public String getFullJavaType() {
        return fullJavaType;
    }

    public String getJdbcType() {
        return jdbcType;
    }

}
