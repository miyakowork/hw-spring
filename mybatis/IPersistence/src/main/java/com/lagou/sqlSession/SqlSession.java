package com.lagou.sqlSession;

import java.util.List;

public interface SqlSession {

    //查询所有
    public <E> List<E> selectList(String statementid, Object... params) throws Exception;

    //根据条件查询单个
    public <T> T selectOne(String statementid, Object... params) throws Exception;


    //为Dao接口生成代理实现类
    public <T> T getMapper(Class<?> mapperClass);


    //========================================新增方法====================

    /**
     * dml insert、delete、update执行方法
     * @param statementId
     * @param params
     * @return
     * @throws Exception
     */
    int update(String statementId,Object... params)throws Exception;
    /**
     * 增强getMapper方法
     * 根据xml的标签类型调用不同的查询方法类型
     *
     * @param mapperClass Mapper接口类
     * @param <T>
     * @return
     * @throws Exception
     */
    <T> T getMapperExt(Class<T> mapperClass) throws Exception;

}
