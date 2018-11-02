<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="${mapper}">

    <resultMap id="BaseResultMap" type="${entity}">
        <id column="${pkField.fieldName}" property="${pkField.propertyName}" jdbcType="${pkField.jdbcType}" />
    <#list fieldList as field>
        <result column="${field.fieldName}" property="${field.propertyName}" jdbcType="${field.jdbcType}" />
    </#list>
    </resultMap>

    <sql id="Base_Column_List">
        ${pkField.fieldName}<#list fieldList as field>,${field.fieldName}</#list>
    </sql>

    <sql id="Alias_Column_List">
        t.${pkField.fieldName}<#list fieldList as field>,t.${field.fieldName}</#list>
    </sql>

    <sql id="Base_Condition">
        <where>
        <#list queryFieldList as field>
            <if test="${field.propertyName} != null">and t.${field.fieldName} = ${r"#"}{${field.propertyName},jdbcType=${field.jdbcType}}</if>
        </#list>
        </where>
        <!-- 自定义 -->
    </sql>

    <!-- 通过主键查询对象 -->
    <select id="selectByPrimaryKey" resultMap="BaseResultMap" parameterType="${pkField.fullJavaType}">
        select
        <include refid="Base_Column_List" />
        from ${tableName}
        where ${pkField.fieldName} = ${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}}
    </select>

    <!-- 通过主键删除对象 -->
    <delete id="deleteByPrimaryKey" parameterType="${pkField.fullJavaType}">
        delete from ${tableName}
        where ${pkField.fieldName} = ${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}}
    </delete>

    <!-- 新增对象(所有字段) -->
    <insert id="insert" parameterType="${entity}">
        <selectKey resultType="${pkField.fullJavaType}" order="AFTER" keyProperty="${pkField.fieldName}">
            SELECT LAST_INSERT_ID() AS ${pkField.fieldName}
        </selectKey>

        insert into ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="${pkField.propertyName} != null">${pkField.fieldName},</if>
        <#list fieldList as field>
            <if test="${field.propertyName} != null">${field.fieldName},</if>
        </#list>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
        <#if pkField.autoIncrement == true >
            <if test="${pkField.propertyName} != null">${pkField.fieldName},</if>
        <#else>
            <if test="${pkField.propertyName} != null">${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}},</if>
        </#if>
        <#list fieldList as field>
            <if test="${field.propertyName} != null">${r"#"}{${field.propertyName},jdbcType=${field.jdbcType}},</if>
        </#list>
        </trim>
    </insert>

    <!-- 新增对象(部分字段) -->
    <insert id="insertSelective" parameterType="${entity}">
        <selectKey resultType="${pkField.fullJavaType}" order="AFTER" keyProperty="${pkField.fieldName}">
            SELECT LAST_INSERT_ID() AS ${pkField.fieldName}
        </selectKey>

        insert into ${tableName}
        <trim prefix="(" suffix=")" suffixOverrides=",">
            <if test="${pkField.propertyName} != null">${pkField.fieldName},</if>
        <#list fieldList as field>
            <if test="${field.propertyName} != null">${field.fieldName},</if>
        </#list>
        </trim>
        <trim prefix="values (" suffix=")" suffixOverrides=",">
        <#if pkField.autoIncrement == true >
            <if test="${pkField.propertyName} != null">${pkField.fieldName},</if>
        <#else>
            <if test="${pkField.propertyName} != null">${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}},</if>
        </#if>
        <#list fieldList as field>
            <if test="${field.propertyName} != null">${r"#"}{${field.propertyName},jdbcType=${field.jdbcType}},</if>
        </#list>
        </trim>
    </insert>

    <!-- 修改对象 (部分字段)-->
    <update id="updateByPrimaryKeySelective" parameterType="${entity}">
        update ${tableName}
        <set>
        <#list fieldList as field>
            <if test="${field.propertyName} != null">${field.fieldName} = ${r"#"}{${field.propertyName},jdbcType=${field.jdbcType}},</if>
        </#list>
        </set>
        where ${pkField.fieldName} = ${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}}
    </update>

    <!-- 修改对象(所有字段) -->
    <update id="updateByPrimaryKey" parameterType="com.kbao.nmi.order.entity.InsureOrder">
        update ${tableName}
        set
        <#list fieldList as field>
            ${field.fieldName} = ${r"#"}{${field.propertyName},jdbcType=${field.jdbcType}}<#if field_has_next>,</#if>
        </#list>
        where ${pkField.fieldName} = ${r"#"}{${pkField.propertyName},jdbcType=${pkField.jdbcType}}
    </update>

    <!-- 通过复合条件查询 -->
    <select id="selectAll" resultMap="BaseResultMap" parameterType="java.util.HashMap">
        select
        <include refid="Alias_Column_List" />
        from ${tableName} t
        <include refid="Base_Condition" />
    </select>

    <!-- 通过复合条件查询 -->
    <select id="selectAllToMap" resultType="Map" parameterType="java.util.HashMap">
        select
        <include refid="Alias_Column_List" />
        from ${tableName} t
        <include refid="Base_Condition" />
    </select>

    <!-- 通过复合条件查询 -->
    <select id="list" resultMap="BaseResultMap" parameterType="java.util.HashMap">
        select
        <include refid="Alias_Column_List" />
        from ${tableName} t
        <include refid="Base_Condition" />
    </select>

    <!-- 批量插入(所有字段) -->
    <insert id="batchInsert" parameterType="java.util.List">
        insert into ${tableName} (
            <include refid="Base_Column_List" />
        )
        values
        <foreach collection="list" index="index" item="item" separator=",">
        (
            ${r"#"}{item.${pkField.propertyName}}
        <#list fieldList as field>
            ${r"<choose><when"} test="item.${field.propertyName} !=null">,${r"#"}{item.${field.propertyName}}</when><otherwise>,default</otherwise></choose>
        </#list>
        )
        </foreach>
    </insert>

    <!-- 批量插入或更新(所有字段) -->
    <update id="batchUpdate" parameterType="java.util.List">
        insert into ${tableName}(
            <include refid="Base_Column_List" />
        )
        values
        <foreach collection="list" index="index" item="item" separator=",">
        (
            ${r"#"}{item.${pkField.propertyName}}
        <#list fieldList as field>
            ${r"<choose><when"} test="item.${field.propertyName} !=null">,${r"#"}{item.${field.propertyName}}</when><otherwise>,default</otherwise></choose>
        </#list>
        )
        </foreach>
        on duplicate key update <#list fieldList as field>${field.fieldName} = values(${field.fieldName})<#if field_has_next>,</#if> </#list>
    </update>

    <!-- 批量删除 -->
    <delete id="batchDelete" parameterType="java.util.List">
        delete from ${tableName} where id in
        <foreach collection="list" index="index" item="item" open="(" separator="," close=")">
            ${r"#{item}"}
        </foreach>
    </delete>

    <!-- 自定义 -->

</mapper>
