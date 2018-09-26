package ${mapperClassPackage};

import org.apache.ibatis.annotations.Mapper;
import com.kbao.core.common.web.dao.sql.BaseMapper;
import ${entityPackage}.${entityName};
import java.util.List;
import java.util.Map;

<#if tableComment??>
/**
 * ${tableComment}Mapper对象
 **/
</#if>
@Mapper
public interface ${entityName}Mapper extends BaseMapper<${entityName}, ${idJavaType}> {

    List<${entityName}> list(Map<String, Object> queryParam);

    int batchDelete(List<String> ids);
}