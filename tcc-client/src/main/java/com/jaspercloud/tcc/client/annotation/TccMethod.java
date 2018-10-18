package com.jaspercloud.tcc.client.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TccMethod {

    String uniqueName();

    String confirmMethod() default "";

    String cancelMethod() default "";

    long timeout() default 10 * 1000;
}
