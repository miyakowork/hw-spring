package com.lagou.edu.utils;

import com.alibaba.druid.pool.DruidDataSource;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.annotation.dependency.Autowired;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author 应癫
 */
@Component
public class ConnectionUtils {

    @Autowired
    private DruidDataSource druidDataSource;


    private final ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    /**
     * 从当前线程获取连接
     */
    public Connection getCurrentThreadConn() throws SQLException {
        /**
         * 判断当前线程中是否已经绑定连接，如果没有绑定，需要从连接池获取一个连接绑定到当前线程
         */
        Connection connection = threadLocal.get();
        if (connection == null) {
            // 从连接池拿连接并绑定到线程
            connection = druidDataSource.getConnection();
            // 绑定到当前线程
            threadLocal.set(connection);
        }
        return connection;
    }
}
