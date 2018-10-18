package com.jaspercloud.tcc.core.redis;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Created by TimoRD on 2017/12/20.
 */
public class BytesRedisTemplate extends RedisTemplate<String, byte[]> {

    public BytesRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        BytesRedisSerializer bytesRedisSerializer = new BytesRedisSerializer();
        setKeySerializer(stringSerializer);
        setHashKeySerializer(stringSerializer);
        setValueSerializer(bytesRedisSerializer);
        setHashValueSerializer(bytesRedisSerializer);
        setConnectionFactory(redisConnectionFactory);
    }
}
