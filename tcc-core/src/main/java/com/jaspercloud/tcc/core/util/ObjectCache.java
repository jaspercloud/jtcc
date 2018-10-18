package com.jaspercloud.tcc.core.util;

import java.util.Map;

public final class ObjectCache {

    private ObjectCache() {

    }

    public static <K, V> V get(Map<K, V> map, K key, Callback<V> callback) throws Exception {
        return get(map, key, true, callback);
    }

    public static <K, V> V get(Map<K, V> map, K key, boolean allowNull, Callback<V> callback) throws Exception {
        V obj = map.get(key);
        if (null == obj) {
            obj = callback.call();
            if (null == obj && !allowNull) {
                obj = (V) new Null();
            }
            map.put(key, obj);
        }
        if (Null.class.isInstance(obj)) {
            return null;
        }
        return obj;
    }

    private static class Null {

    }

    public interface Callback<V> {

        V call() throws Exception;
    }
}
