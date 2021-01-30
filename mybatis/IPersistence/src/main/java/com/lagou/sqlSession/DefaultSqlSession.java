package com.lagou.sqlSession;

import com.lagou.enums.MethodType;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.lang.reflect.*;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementid, Object... params) throws Exception {

        //将要去完成对simpleExecutor里的query方法的调用
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementid);
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);

        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(String statementid, Object... params) throws Exception {
        List<Object> objects = selectList(statementid, params);
        if (objects.size() == 1) {
            return (T) objects.get(0);
        } else {
            throw new RuntimeException("查询结果为空或者返回结果过多");
        }
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        // 使用JDK动态代理来为Dao接口生成代理对象，并返回

        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // 底层都还是去执行JDBC代码 //根据不同情况，来调用selctList或者selectOne
                // 准备参数 1：statmentid :sql语句的唯一标识：namespace.id= 接口全限定名.方法名
                // 方法名：findAll
                String methodName = method.getName();
                String className = method.getDeclaringClass().getName();

                String statementId = className + "." + methodName;

                // 准备参数2：params:args
                // 获取被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                // 判断是否进行了 泛型类型参数化
                if (genericReturnType instanceof ParameterizedType) {
                    List<Object> objects = selectList(statementId, args);
                    return objects;
                }

                return selectOne(statementId, args);

            }
        });

        return (T) proxyInstance;
    }

    //=========================新增方法=======================

    /**
     * dml insert、delete、update执行方法
     *实际的调用方法
     * @param statementId
     * @param params
     * @return
     * @throws Exception
     */
    @Override
    public int update(String statementId, Object... params) throws Exception {
        //调用simpleExecutor
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        return simpleExecutor.update(configuration, mappedStatement, params);
    }

    /**
     * 根据xml的标签类型调用不同的查询方法类型
     *
     * @param mapperClass Mapper接口类
     * @return
     */
    @Override
    public <T> T getMapperExt(Class<T> mapperClass) {
        //使用JDK的动态代理生成动态代理类
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, (proxy, method, args) -> {
            //获取statementId
            String methodName = method.getName();
            String className = method.getDeclaringClass().getName();
            String statementId = className + "." + methodName;
            return execute(method, args, statementId);
        });
    }


    /**
     * 通用执行方法：即 select、insert、update、delete全部往此方法走
     * 通过此方法来判断具体走哪个实现方法
     *
     * @param method
     * @param args
     * @param statementId
     * @return
     * @throws Exception
     */
    private Object execute(Method method, Object[] args, String statementId) throws Exception {
        //调用simpleExecutor
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementId);
        MethodType executeType = mappedStatement.getExecuteType();
        if (MethodType.SELECT.equals(executeType)) {
            // 获取被调用方法的返回值类型，如果是list 则执行selectList
            Type genericReturnType = method.getGenericReturnType();
            // 判断是否进行了 泛型类型参数化
            if (genericReturnType instanceof ParameterizedType) {
                return selectList(statementId, args);
            }
            return selectOne(statementId, args);
        }
        //除了select就是目前作业项目中就是insert、delete和update
        //都执行 update底层方法
        else {
            return update(statementId, args);
        }
    }

}
