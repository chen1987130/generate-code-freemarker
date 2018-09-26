package ${servicePackage};

import org.springframework.stereotype.Service;
import com.kbao.core.common.web.service.sql.BaseSQLServiceImpl;
import ${entityPackage}.${entityName};
import ${mapperClassPackage}.${entityName}Mapper;
import java.util.List;
import java.util.Map;

<#if tableComment??>
/**
 * ${tableComment}Service对象
 **/
</#if>
@Service
public class ${entityName}Service extends BaseSQLServiceImpl<${entityName}, ${idJavaType}, ${entityName}Mapper> {

    public List<${entityName}> list(Map<String, Object> queryParam){
        return this.mapper.list(queryParam);
    }

    public int batchDelete(List<String> ids){
        return this.mapper.batchDelete(ids);
    }

}