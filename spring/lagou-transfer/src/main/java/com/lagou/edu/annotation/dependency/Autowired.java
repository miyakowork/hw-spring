package com.lagou.edu.annotation.dependency;

import java.lang.annotation.*;

/**
 * @author wuwenbin
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {

    boolean required() default true;

    /**
     * 这里我们加一个别名的定义
     * 把 spring 中的类似 @Qualifier 合并
     *
     * @return
     */

    String value() default "";
}
