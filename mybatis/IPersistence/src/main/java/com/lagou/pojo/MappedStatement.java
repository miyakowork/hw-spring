package com.lagou.pojo;

import com.lagou.enums.MethodType;

public class MappedStatement {

    //id标识
    private String id;
    //返回值类型
    private String resultType;
    //参数值类型
    private String parameterType;

    //sql语句
    private String sql;

    /**
     * 新增属性，方便之后的方法类型识别
     * 执行类型：select、update、delete、insert等（此示例使用此4中示例）
     */
    private MethodType executeType;
    /**
     * 是否需要返回自增主键（如果为insert方法）
     */
    private Boolean useGeneratedKeys;

    /**
     * 主键在java pojo 中的属性名，默认id
     */
    private String pkJavaProp = "id";

    public MethodType getExecuteType() {
        return executeType;
    }

    public void setExecuteType(MethodType executeType) {
        this.executeType = executeType;
    }

    public Boolean getUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(Boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }


    public String getPkJavaProp() {
        return pkJavaProp;
    }

    public void setPkJavaProp(String pkJavaProp) {
        this.pkJavaProp = pkJavaProp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
