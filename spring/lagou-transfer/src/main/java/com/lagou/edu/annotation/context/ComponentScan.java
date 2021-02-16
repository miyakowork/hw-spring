package com.lagou.edu.annotation.context;

import java.lang.annotation.*;

/**
 * @author wuwenbin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ComponentScan {

    /**
     * 扫描的包路径基础路径
     * 即扫描该配置包名及其子包下的所有 class
     *
     * @return
     */
    String[] value() default {};
}
