package com.lagou.dao;

import com.lagou.pojo.User;

import java.util.List;

public interface IUserDao {

    //查询所有用户
    public List<User> findAll() throws Exception;


    //根据条件进行用户查询
    public User findByCondition(User user) throws Exception;


    //===============新增方法==============

    User findById(User user) throws Exception;

    /**
     * 根据id修改行吗
     *
     * @param user
     * @return
     * @throws Exception
     */
    int updateNameById(User user) throws Exception;

    /**
     * 插入一个user对象
     *
     * @param user
     * @return
     * @throws Exception
     */
    int insertUser(User user) throws Exception;

    /**
     * 删除用户
     * @param user
     * @return
     * @throws Exception
     */
    int deleteById(User user) throws Exception;

}
