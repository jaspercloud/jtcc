package com.jaspercloud.tcc.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassCache {

    private ClassCache() {

    }

    private static Map<String, Class<?>> classMap = new ConcurrentHashMap<>();

    public static Class<?> getClass(String className) throws Exception {
        Class<?> clazz = getClass(Thread.currentThread().getContextClassLoader(), className);
        return clazz;
    }

    public static Class<?> getClass(ClassLoader classLoader, String className) throws Exception {
        Class<?> clazz = ObjectCache.get(classMap, className, new ObjectCache.Callback<Class<?>>() {
            @Override
            public Class<?> call() throws Exception {
                Class<?> clazz = classLoader.loadClass(className);
                return clazz;
            }
        });
        return clazz;
    }
}
