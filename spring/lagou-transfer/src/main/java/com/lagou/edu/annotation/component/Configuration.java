package com.lagou.edu.annotation.component;

import java.lang.annotation.*;

/**
 * 标识为配置类
 * 有限注入此类的属性，比如预先定义好的 DataSource 等
 *
 * @author wuwenbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Configuration {

    String value() default "";
}
