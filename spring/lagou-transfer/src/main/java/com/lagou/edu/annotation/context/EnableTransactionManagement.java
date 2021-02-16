package com.lagou.edu.annotation.context;

import java.lang.annotation.*;

/**
 * 注解声明是否开启事务管理
 *
 * @author wuwenbin
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableTransactionManagement {
}
