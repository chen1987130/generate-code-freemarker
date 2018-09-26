package ${entityPackage};

import java.io.Serializable;
import java.util.Date;

<#if tableComment??>
/**
 * ${tableComment}实体对象
 **/
</#if>
public class ${entityName} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list fieldList as field>
    <#if (field.comment)??>
    /**
     * ${field.comment}
     **/
    </#if>
    private ${field.javaType} ${field.propertyName};

</#list>
<#list fieldList as field>
    public ${field.javaType} get${field.propertyName?cap_first}(){
        return this.${field.propertyName};
    }

    public void set${field.propertyName?cap_first}(${field.javaType} ${field.propertyName}){
        this.${field.propertyName} = ${field.propertyName};
    }

</#list>

}