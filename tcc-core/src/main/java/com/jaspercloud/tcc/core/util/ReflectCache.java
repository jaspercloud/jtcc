package com.jaspercloud.tcc.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ReflectCache {

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, String> TYPE_STR_CACHE = new ConcurrentHashMap<>();

    private ReflectCache() {

    }

    /**
     * 放入类描述缓存
     *
     * @param clazz   类
     * @param typeStr 对象描述
     */
    public static void putTypeStrCache(Class clazz, String typeStr) {
        TYPE_STR_CACHE.put(clazz, typeStr);
    }

    /**
     * 得到类描述缓存
     *
     * @param clazz 类
     * @return 类描述
     */
    public static String getTypeStrCache(Class clazz) {
        return TYPE_STR_CACHE.get(clazz);
    }

    /**
     * 放入Class缓存
     *
     * @param typeStr 对象描述
     * @param clazz   类
     */
    public static void putClassCache(String typeStr, Class clazz) {
        CLASS_CACHE.put(typeStr, clazz);
    }

    /**
     * 得到Class缓存
     *
     * @param typeStr 对象描述
     * @return 类
     */
    public static Class getClassCache(String typeStr) {
        return CLASS_CACHE.get(typeStr);
    }
}
