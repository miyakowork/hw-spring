package com.lagou.enums;


public enum MethodType {
    /**
     * 目前示例演示此4种
     */
    SELECT,
    UPDATE,
    INSERT,
    DELETE;

    public static MethodType getTypeByXmlTag(String xmlTag) {
        switch (xmlTag) {
            case "select":
                return MethodType.SELECT;
            case "update":
                return MethodType.UPDATE;
            case "insert":
                return MethodType.INSERT;
            case "delete":
                return MethodType.DELETE;
            default:
                throw new RuntimeException("未识别的方法");
        }
    }
}
