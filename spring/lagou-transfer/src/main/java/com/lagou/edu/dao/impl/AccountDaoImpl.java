package com.lagou.edu.dao.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.lagou.edu.annotation.dependency.Autowired;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.dao.AccountDao;
import com.lagou.edu.pojo.Account;
import com.lagou.edu.utils.ConnectionUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * 改造下相关属性为注入
 *
 * @author 应癫
 */
@Component("accountDao")
public class AccountDaoImpl implements AccountDao {

    /**
     * 注入已存在工厂中的属性
     */
    @Autowired
    private ConnectionUtils connectionUtils;
//    @Autowired
//    private DruidDataSource druidDataSource;

    @Override
    public Account queryAccountByCardNo(String cardNo) throws Exception {
        //使用工厂中的bean
        Connection con = connectionUtils.getCurrentThreadConn();
//        Connection con = druidDataSource.getConnection();
        String sql = "select * from account where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, cardNo);
        ResultSet resultSet = preparedStatement.executeQuery();

        Account account = new Account();
        while (resultSet.next()) {
            account.setCardNo(resultSet.getString("cardNo"));
            account.setName(resultSet.getString("name"));
            account.setMoney(resultSet.getInt("money"));
        }

        resultSet.close();
        preparedStatement.close();
        return account;
    }

    @Override
    public int updateAccountByCardNo(Account account) throws Exception {
        // 从连接池获取连接
        //使用工厂中的bean
        Connection con = connectionUtils.getCurrentThreadConn();
//        Connection con = druidDataSource.getConnection();
        String sql = "update account set money=? where cardNo=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1, account.getMoney());
        preparedStatement.setString(2, account.getCardNo());
        int i = preparedStatement.executeUpdate();
        preparedStatement.close();
        return i;
    }

}
