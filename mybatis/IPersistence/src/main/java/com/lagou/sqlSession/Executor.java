package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.util.List;

public interface Executor {

    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws Exception;

    /**
     * DML操作（带事物的：insert、update、delete等）
     * @param configuration
     * @param ms
     * @param params
     * @return
     * @throws Exception
     */
    int update(Configuration configuration,MappedStatement ms, Object... params) throws Exception;
}
