package com.jaspercloud.tcc.core.util;

/**
 * Created by TimoRD on 2017/5/10.
 */
public final class RedisKeyUtil {

    private RedisKeyUtil() {

    }

    public static String getRedisKey(String... keys) {
        StringBuilder builder = new StringBuilder();
        for (String key : keys) {
            builder.append(key);
            builder.append(":");
        }
        builder.deleteCharAt(builder.length() - 1);
        String redisKey = builder.toString();

        return redisKey;
    }
}
