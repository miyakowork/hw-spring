package com.lagou.edu.annotation.component;

import java.lang.annotation.*;

/**
 * 在本作业的示例工程中就不使用 Service 和 Repository 分层注解了，
 * 效果其实是类似的，本作业简单实例用此注解代表
 *
 * @author wuwenbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {

    String value() default "";
}
