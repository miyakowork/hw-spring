package com.lagou.sqlSession;


import com.lagou.config.BoundSql;
import com.lagou.enums.MethodType;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.utils.GenericTokenParser;
import com.lagou.utils.ParameterMapping;
import com.lagou.utils.ParameterMappingTokenHandler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements Executor {


    @Override                                                                                //user
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws Exception {
        // 1. 注册驱动，获取连接
        Connection connection = configuration.getDataSource().getConnection();

        // 2. 获取sql语句 : select * from user where id = #{id} and username = #{username}
        //转换sql语句： select * from user where id = ? and username = ? ，转换的过程中，还需要对#{}里面的值进行解析存储
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);

        // 3.获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());

        // 4. 设置参数
        //获取到了参数的全路径
        setExecuteParam(mappedStatement, boundSql, preparedStatement, params);


        // 5. 执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        String resultType = mappedStatement.getResultType();
        Class<?> resultTypeClass = getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        // 6. 封装返回结果集
        while (resultSet.next()) {
            Object o = resultTypeClass.newInstance();
            //元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {

                // 字段名
                String columnName = metaData.getColumnName(i);
                // 字段的值
                Object value = resultSet.getObject(columnName);

                //使用反射或者内省，根据数据库表和实体的对应关系，完成封装
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(o, value);


            }
            objects.add(o);

        }
        return (List<E>) objects;

    }


    /**
     * DML操作（insert、update、delete等）
     *
     * @param configuration
     * @param ms
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public int update(Configuration configuration, MappedStatement ms, Object... params) throws Exception {

        //1、TODO:此处是要执行删除缓存的操作，此处自定义的框架未涉及
        // clearLocalCache();


        //2、开始执行DML的相关操作，此处我们使用PreparedStatement
        ////2.1、首先我们与准备下一些属性操作、connection、执行sql、和参数等
        Connection connection = configuration.getDataSource().getConnection();
        String sql = ms.getSql();
        BoundSql boundSql = getBoundSql(sql);
        ////2.2、获取执行预处理对象
        //////判断是否为insert执行方法
        boolean isInsertMethod = ms.getExecuteType().equals(MethodType.INSERT);
        //////如果是insert方法，拉取xml属性是否有配置需要返回自增主键
        /////TODO:此处可能需要去针对不同的数据库来操作（orcale和mysql 主键处理就不同）
        Boolean useGeneratedKeys = ms.getUseGeneratedKeys();
        PreparedStatement ps;
        if (isInsertMethod && useGeneratedKeys) {
            ps = connection.prepareStatement(boundSql.getSqlText(), Statement.RETURN_GENERATED_KEYS);
        } else {
            ps = connection.prepareStatement(boundSql.getSqlText(), Statement.NO_GENERATED_KEYS);
        }
        ////2.3、设置参数
        setExecuteParam(ms, boundSql, ps, params);
        ////2.4、执行DML语句
        ps.executeUpdate();
        if (isInsertMethod && useGeneratedKeys) {
            ////2.4.1、看看有没有自增主键返回，如果有设置到实体中
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                //可能有多个，但是我们规定约束下插入实体的属性放在取第一个就好
                if (params != null && params.length > 0) {
                    // java 类中 主键属性字段名
                    String columnName = ms.getPkJavaProp();
                    //获取参数实例
                    Class<?> parameterTypeClass = params[0].getClass();
                    //获取自增的主键值
                    Object value = rs.getInt(1);
                    //使用反射或者内省，根据数据库表和实体的对应关系，完成封装
                    PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, parameterTypeClass);
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    writeMethod.invoke(params[0], value);
                }
            }
        }
        ////2.5、返回执行的结果影响行数
        int affectRows = ps.getUpdateCount();
        ps.close();
        connection.close();

        //3、返回执行影响的条数
        return affectRows;
    }

    private void setExecuteParam(MappedStatement ms,
                                 BoundSql boundSql,
                                 PreparedStatement ps,
                                 Object... params) throws Exception {
        String parameterType = ms.getParameterType();
        Class<?> parameterTypeClass = getClassType(parameterType);
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            String content = parameterMapping.getContent();
            //反射
            Field declaredField = parameterTypeClass.getDeclaredField(content);
            //暴力访问
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);
            ps.setObject(i + 1, o);

        }
    }


    //======================= 私有解析方法 ==========================

    private Class<?> getClassType(String paramterType) throws ClassNotFoundException {
        if (paramterType != null) {
            Class<?> aClass = Class.forName(paramterType);
            return aClass;
        }
        return null;

    }


    /**
     * 完成对#{}的解析工作：1.将#{}使用？进行代替，2.解析出#{}里面的值进行存储
     *
     * @param sql
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        //标记处理类：配置标记解析器来完成对占位符的解析处理工作
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{", "}", parameterMappingTokenHandler);
        //解析出来的sql
        String parseSql = genericTokenParser.parse(sql);
        //#{}里面解析出来的参数名称
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();

        BoundSql boundSql = new BoundSql(parseSql, parameterMappings);
        return boundSql;

    }


}
