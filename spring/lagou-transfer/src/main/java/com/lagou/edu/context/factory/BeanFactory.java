package com.lagou.edu.context.factory;

/**
 * 定义一些 bean 工厂的基础公共功能
 *
 * @author wuwenbin
 */
public interface BeanFactory {

    boolean containsBean(String beanName);

    <T> T getBean0(String beanName);

    <T> T getBean0(Class<T> clazz);
}
