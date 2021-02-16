package com.lagou.edu.context.factory.proxy;

import cn.hutool.core.util.ArrayUtil;
import com.lagou.edu.annotation.aop.Transactional;
import com.lagou.edu.annotation.component.Component;
import com.lagou.edu.annotation.dependency.Autowired;
import com.lagou.edu.tx.TransactionManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * @author 应癫
 * <p>
 * <p>
 * 代理对象工厂：生成代理对象的
 */

@Component
public class ProxyFactory {


    @Autowired
    private TransactionManager transactionManager;


    @SuppressWarnings("unchecked")
    public <T> T createTxProxy(Object obj) {
        Class<?>[] interfaces = obj.getClass().getInterfaces();
        if (ArrayUtil.isNotEmpty(interfaces)) {
            return (T) createTransactionalProxyByCglib(obj);
        } else {
            return (T) createTransactionalProxyByJdkDynamic(obj);
        }
    }

    /**
     * Jdk动态代理
     *
     * @param obj 委托对象
     * @return 代理对象
     */
    private Object createTransactionalProxyByJdkDynamic(Object obj) {

        // 获取代理对象
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return methodInvoke(method, args, obj);
                    }
                });

    }


    /**
     * 使用cglib动态代理生成代理对象
     *
     * @param obj 委托对象
     * @return
     */
    private Object createTransactionalProxyByCglib(Object obj) {
        return Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                return methodInvoke(method, objects, obj);
            }
        });
    }


    private Object methodInvoke(Method method, Object[] args, Object obj) throws Exception {
        Object result;
        boolean transactionalAnnotationPresent = method.isAnnotationPresent(Transactional.class);
        transactionalAnnotationPresent = transactionalAnnotationPresent || obj.getClass().isAnnotationPresent(Transactional.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        if (transactional == null) {
            transactional = obj.getClass().getAnnotation(Transactional.class);
        }
        Class<?>[] rollbackClasses = transactional.rollbackForClass();
        if (transactionalAnnotationPresent) {
            try {
                // 开启事务(关闭事务的自动提交)
                transactionManager.beginTransaction();
                //反射执行原方法
                result = method.invoke(obj, args);
                // 提交事务
                transactionManager.commit();
            } catch (Throwable e) {
                e.printStackTrace();
                // 回滚事务
                if (Arrays.stream(rollbackClasses).anyMatch(rollbackClass -> rollbackClass.isAssignableFrom(e.getClass()))) {
                    transactionManager.rollback();
                }
                //如果回滚类异常类不是注解中标识的类或者是子类，那么就不会滚，还是照常提交
                else {
                    transactionManager.commit();
                }
                // 抛出异常便于上层servlet捕获
                throw e;
            }
        } else {
            result = method.invoke(obj, args);
        }
        return result;
    }

}