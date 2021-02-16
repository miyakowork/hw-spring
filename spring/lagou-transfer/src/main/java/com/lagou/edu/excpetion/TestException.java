package com.lagou.edu.excpetion;

/**
 * 测试异常回滚类
 *
 * @author wuwenbin
 */
public class TestException extends RuntimeException {
    public TestException() {
        super("test exception");
    }
}
