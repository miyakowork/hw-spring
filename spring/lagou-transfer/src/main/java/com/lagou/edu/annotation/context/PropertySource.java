package com.lagou.edu.annotation.context;

import java.lang.annotation.*;

/**
 * @author wuwenbin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropertySource {
    /**
     * 默认为 classpath 下的 config.properties 文件
     *
     * @return
     */
    String[] value() default "config.properties";
}
