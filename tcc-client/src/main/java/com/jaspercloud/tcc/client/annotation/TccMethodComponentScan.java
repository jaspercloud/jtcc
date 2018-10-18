package com.jaspercloud.tcc.client.annotation;

import com.jaspercloud.tcc.client.autoconfig.TccMethodComponentScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(TccMethodComponentScanRegistrar.class)
public @interface TccMethodComponentScan {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};
}
