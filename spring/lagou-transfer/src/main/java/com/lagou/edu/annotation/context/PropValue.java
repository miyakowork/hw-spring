package com.lagou.edu.annotation.context;

import java.lang.annotation.*;

/**
 * 配置文件的属性注入
 *
 * @author wuwenbin
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PropValue {

    /**
     * 这里我们加一个别名的定义
     * 把 spring 中的类似 @Qualifier 合并
     *
     * @return
     */

    String value();
}
