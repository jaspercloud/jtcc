package com.japsercloud.tcc.server.support;

import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisLockSupport {

    private StringRedisTemplate redisTemplate;

    public RedisLockSupport(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Lock newLock(String lockKey, long timeout) {
        return new RedisLockImpl(redisTemplate, lockKey, timeout);
    }
}
