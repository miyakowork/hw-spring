package com.lagou.edu.tx;

/**
 * @author wuwenbin
 */
public interface TransactionManager {

    /**
     * 开启事务
     *
     * @throws Exception
     */
    void beginTransaction() throws Exception;

    /**
     * 提交事务
     *
     * @throws Exception
     */
    void commit() throws Exception;

    /**
     * 回滚事务
     *
     * @throws Exception
     */
    void rollback() throws Exception;
}
