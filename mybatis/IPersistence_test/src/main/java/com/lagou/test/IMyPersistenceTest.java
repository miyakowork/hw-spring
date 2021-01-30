package com.lagou.test;

import com.lagou.dao.IUserDao;
import com.lagou.io.Resources;
import com.lagou.pojo.User;
import com.lagou.sqlSession.SqlSession;
import com.lagou.sqlSession.SqlSessionFactory;
import com.lagou.sqlSession.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class IMyPersistenceTest {

    IUserDao userDao;


    @Before
    public void before() throws Exception {
        InputStream resourceAsSteam = Resources.getResourceAsSteam("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsSteam);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        userDao = sqlSession.getMapperExt(IUserDao.class);
    }

    @Test
    public void testSelect() throws Exception {
        System.out.println("============ select 测试 ================");
        User queryById = new User();
        queryById.setId(1);

        List<User> all = userDao.findAll();
        soutList(all);
        System.out.println("============ select 测试结束 ================");

    }


    @Test
    public void testUpdate() throws Exception {
        System.out.println("============ update 测试 ================");
        User queryById = new User();
        queryById.setId(1);
        User updateNameById = new User();
        updateNameById.setId(1);
        updateNameById.setUsername("wuwenbin_edit_test" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));

        System.out.println("修改前：" + userDao.findById(queryById));
        userDao.updateNameById(updateNameById);
        System.out.println("修改后：" + userDao.findById(queryById));
        System.out.println("============ update 测试结束 ================");
    }


    @Test
    public void testInsert() throws Exception {
        System.out.println("============ insert 测试 ================");
        User insert = new User();
        insert.setUsername("wuwenbin_edit_test" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
        insert.setBirthday("2000-01-01");
        insert.setPassword("123456");

        userDao.insertUser(insert);
        System.out.println("刚刚插入的user：" + insert);
        System.out.println("数据库中所有的user：");
        soutList(userDao.findAll().stream().sorted(Comparator.comparing(User::getId).reversed()).collect(Collectors.toList()));
        System.out.println("============ insert 测试结束 ================");
    }


    @Test
    public void testDelete() throws Exception {
        System.out.println("============ delete 测试 ================");

        //找出数据库id max 的那条并作为删除条件
        List<User> userList = userDao.findAll().stream().sorted(Comparator.comparing(User::getId).reversed()).collect(Collectors.toList());
        User delete = new User();
        delete.setId(userList.get(0).getId());

        System.out.println("删除对象：" + userList.get(0));
        System.out.println("删除之前，用户总数：" + userList.size());
        userDao.deleteById(delete);
        System.out.println("删除之后，用户总数：" + userDao.findAll().size());
        System.out.println("============ delete 测试结束 ================");
    }


    private void soutList(List<User> all) {
        for (User user1 : all) {
            System.out.println(user1);
        }
    }
}
