package com.lagou.edu.annotation.aop;

import java.lang.annotation.*;

/**
 * 本作业中的事务的注解只采用方法上的注解上来讲解。
 * 此处去除了接口类以及实现类上的注解功能，如果需要只需多加上多个优先级的判断覆盖即可
 *
 * @author wuwenbin
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {

    /**
     * 同样的注解中的一些属性也没有全部实现
     * 此处用了几个代表性的属性来
     * 如 事务管理器、rollbackForClass
     * 其余的如 只读、隔离级别、传播性质等此处省略
     */

    /**
     * 默认事务管理器的 bean name
     *
     * @return
     */
    String value() default "transactionManager";

    /**
     * 默认回滚的异常类 RuntimeException
     *
     * @return
     */
    Class<?>[] rollbackForClass() default {};
}
