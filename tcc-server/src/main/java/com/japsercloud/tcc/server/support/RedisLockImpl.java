package com.japsercloud.tcc.server.support;

import com.jaspercloud.tcc.core.util.RedisKeyUtil;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

public class RedisLockImpl implements Lock {

    private StringRedisTemplate redisTemplate;
    private String lockKey;
    private long timeout;

    public RedisLockImpl(StringRedisTemplate redisTemplate, String lockKey, long timeout) {
        this.redisTemplate = redisTemplate;
        this.lockKey = RedisKeyUtil.getRedisKey("tcc", "lock", lockKey);
        this.timeout = timeout;
    }

    @Override
    public boolean isLock() {
        ValueOperations<String, String> forValue = redisTemplate.opsForValue();
        String result = forValue.get(lockKey);
        long lastLockTime = NumberUtils.toLong(result, 0);
        if (System.currentTimeMillis() < lastLockTime) {
            return true;
        }
        return false;
    }

    @Override
    public void lock() {
        long timeout;
        do {
            timeout = tryLock();
        } while (0 != timeout);
    }

    @Override
    public long tryLock() {
        ValueOperations<String, String> forValue = redisTemplate.opsForValue();
        Boolean flag = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                long lockTime = getTime() + timeout;
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = serializer.serialize(String.valueOf(lockTime));
                boolean flag = redisConnection.setNX(lockKey.getBytes(), value);
                return flag;
            }
        });
        if (true == flag) {
            return 0;
        }
        long getLockTime = NumberUtils.toLong(forValue.get(lockKey), 0);
        if (System.currentTimeMillis() > getLockTime) {
            long lockTime = getTime() + timeout;
            long oldLockTime = NumberUtils.toLong(forValue.getAndSet(lockKey, String.valueOf(lockTime)), 0);
            if (oldLockTime == getLockTime) {
                return 0;
            } else {
                long waitTime = System.currentTimeMillis() - oldLockTime;
                return waitTime;
            }
        } else {
            long waitTime = getLockTime - System.currentTimeMillis();
            return waitTime;
        }
    }

    @Override
    public void unlock() {
        redisTemplate.delete(lockKey);
    }

    private long getTime() {
        Long time = redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.time();
            }
        });
        return time;
    }
}
