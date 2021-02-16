package com.lagou.edu.annotation.component;

import java.lang.annotation.*;

/**
 * @author wuwenbin
 */
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * 默认取方法名
     *
     * @return
     */
    String value() default "";
}
