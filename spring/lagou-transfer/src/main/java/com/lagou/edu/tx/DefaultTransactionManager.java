package com.lagou.edu.tx;

import com.alibaba.druid.pool.DruidDataSource;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.annotation.dependency.Autowired;
import com.lagou.edu.utils.ConnectionUtils;

/**
 * @author 应癫
 * <p>
 * 事务管理器类：负责手动事务的开启、提交、回滚
 */
@Component("transactionManager")
public class DefaultTransactionManager implements TransactionManager {

    @Autowired
    private ConnectionUtils connectionUtils;
//    @Autowired
//    private DruidDataSource druidDataSource;


    @Override
    public void beginTransaction() throws Exception {
        connectionUtils.getCurrentThreadConn().setAutoCommit(false);
//        druidDataSource.getConnection().setAutoCommit(false);
    }


    @Override
    public void commit() throws Exception {
        connectionUtils.getCurrentThreadConn().commit();
//        druidDataSource.getConnection().commit();
    }


    @Override
    public void rollback() throws Exception {
        connectionUtils.getCurrentThreadConn().rollback();
//        druidDataSource.getConnection().rollback();
    }
}
