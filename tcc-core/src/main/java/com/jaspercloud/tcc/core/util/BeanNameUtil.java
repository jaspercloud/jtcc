package com.jaspercloud.tcc.core.util;

import org.springframework.util.ClassUtils;

import java.beans.Introspector;

public final class BeanNameUtil {

    private BeanNameUtil() {

    }

    public static String generateBeanName(Class<?> clazz) {
        String shortClassName = ClassUtils.getShortName(clazz.getName());
        String beanName = Introspector.decapitalize(shortClassName);
        return beanName;
    }
}
