package com.lagou.edu.context;

import com.lagou.edu.annotation.aop.Transactional;
import com.lagou.edu.annotation.context.EnableTransactionManagement;
import com.lagou.edu.context.factory.AbstractBeanFactory;
import com.lagou.edu.context.factory.proxy.ProxyFactory;

/**
 * @author wuwenbin
 */
public class MyWebApplicationContext extends AbstractBeanFactory {

    /**
     * 是否启用事务控制
     */
    private final boolean enableTx;

    public MyWebApplicationContext(Class<?> configClass) throws Exception {
        super(configClass);
        this.enableTx = configClass.isAnnotationPresent(EnableTransactionManagement.class);
    }


    /**
     * 清除单例工厂中所有 bean 对象
     */
    public void clearAllBeans() {
        synchronized (singletonBeanPool) {
            singletonBeanPool.clear();
        }
    }

    /**
     * 获取代理的 Bean 对象
     * 自动判断需不需要 AOP 事务代理
     *
     * @param beanName
     * @param <T>
     * @return
     */
    public <T> T getBean(String beanName) {
        Object bean = super.getBean0(beanName);
        if (bean != null) {
            Class<?> beanClass = bean.getClass();
            boolean isPresentTx = beanClass.isAnnotationPresent(Transactional.class);
            if (isPresentTx && enableTx) {
                ProxyFactory proxyFactory = super.getBean0(ProxyFactory.class);
                return proxyFactory.createTxProxy(bean);
            }
        }
        //noinspection unchecked
        return (T) bean;
    }


}
