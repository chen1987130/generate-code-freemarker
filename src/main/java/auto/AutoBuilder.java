package auto;

/**
 * 自动代码生成
 *
 * @author chensheng
 * @version 2015-08-25
 */
public class AutoBuilder {

    // 源文件所在工程目录
    public static final String FILE_DIR = "D:\\gen";

    public static final String PACKAGE_PATH = "com.kbao.nmi.order";

    /**
     * 根据表自行定义
     */
    public static final String[] TABLE_NAME = new String[]{"t_nmi_order"};

    public static final String[] ENTITY_NAME = new String[]{"InsureOrder"};

    /**
     * true:物理删除 false:逻辑删除
     */
    static boolean DELETE_FLAG = true;

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < TABLE_NAME.length; i++) {
            Entity entity = new Entity(FILE_DIR, PACKAGE_PATH);
            entity.init();
            entity.initField(TABLE_NAME[i], ENTITY_NAME[i], DELETE_FLAG);
            entity.buildEntity();
            entity.buildMapper();
            entity.buildMapperClass();
            entity.buildService();
        }
    }
}
