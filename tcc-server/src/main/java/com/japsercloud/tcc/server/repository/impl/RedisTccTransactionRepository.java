package com.japsercloud.tcc.server.repository.impl;

import com.japsercloud.tcc.server.config.TccServerConfigProperties;
import com.japsercloud.tcc.server.repository.TccTransactionInfo;
import com.japsercloud.tcc.server.repository.TccTransactionRepository;
import com.jaspercloud.tcc.core.coordinator.TccMethodData;
import com.jaspercloud.tcc.core.util.ProtobufUtil;
import com.jaspercloud.tcc.core.util.RedisKeyUtil;
import com.jaspercloud.tcc.core.util.TccConstants;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class RedisTccTransactionRepository implements TccTransactionRepository, InitializingBean {

    private static final String TRANSACTION_KEY = "t";
    private static final String METHODS_KEY = "m";
    private static final String METHOD_ID_KEY = "mid";

    private static final String TID_KEY = "tid";
    private static final String CREATE_TIME_KEY = "ct";
    private static final String TIMEOUT_KEY = "t";
    private static final String STATUS_KEY = "s";
    private static final String TIMEOUT_MILLIS_KEY = "tm";
    private static final String COMPENSATE_TIMEOUT_MILLIS_KEY = "cm";

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private TccServerConfigProperties config;

    private RedisAtomicLong redisAtomicLong;

    @Override
    public void afterPropertiesSet() {
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TID_KEY);
        redisAtomicLong = new RedisAtomicLong(redisKey, redisConnectionFactory);
    }

    @Override
    public String createTid(long timeout) {
        String tid = redisAtomicLong.incrementAndGet() + UUID.randomUUID().toString().replaceAll("-", "");
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        long now = System.currentTimeMillis();
        long timeoutMillis = now + timeout;
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, tid);
        Map<String, String> map = new HashMap<>();
        map.put(TID_KEY, tid);
        map.put(CREATE_TIME_KEY, String.valueOf(now));
        map.put(TIMEOUT_KEY, String.valueOf(timeout));
        map.put(STATUS_KEY, TccConstants.TccStatus.Try.toString());
        map.put(TIMEOUT_MILLIS_KEY, String.valueOf(timeoutMillis));
        forHash.putAll(redisKey, map);
        return tid;
    }

    @Override
    public boolean hasTid(String tid) {
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, tid);
        boolean has = redisTemplate.hasKey(redisKey);
        return has;
    }

    @Override
    public void saveTccMethod(TccMethodData tccMethodData) {
        String tid = tccMethodData.getTid();
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), METHODS_KEY, tid);
        long id = createMethodId(tid);
        byte[] bytes = ProtobufUtil.toByteArray(tccMethodData);
        String base64 = Base64Utils.encodeToString(bytes);
        forHash.put(redisKey, String.valueOf(id), base64);
    }

    private Long createMethodId(String tid) {
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, tid);
        Long id = forHash.increment(redisKey, METHOD_ID_KEY, +1);
        return id;
    }

    @Override
    public Map<Long, TccMethodData> getTccMethodList(String tid) {
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), METHODS_KEY, tid);
        Map<String, String> entries = forHash.entries(redisKey);
        Map<Long, TccMethodData> map = new TreeMap<>(new Comparator<Long>() {
            @Override
            public int compare(Long left, Long right) {
                return left.compareTo(right);
            }
        });
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String idString = entry.getKey();
            String value = entry.getValue();
            byte[] bytes = Base64Utils.decodeFromString(value);
            Long id = NumberUtils.toLong(idString, 0);
            TccMethodData tccMethodData = ProtobufUtil.mergeFrom(bytes, new TccMethodData());
            map.put(id, tccMethodData);
        }
        return map;
    }

    @Override
    public void deleteTccMethod(String tid, long methodId) {
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), METHODS_KEY, tid);
        forHash.delete(redisKey, String.valueOf(methodId));
    }

    @Override
    public List<TccTransactionInfo> getTccTransactionList(Long cursor, int limit) {
        List<String> redisKeyList = redisTemplate.execute(new RedisCallback<List<String>>() {
            @Override
            public List<String> doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = (RedisSerializer<String>) redisTemplate.getValueSerializer();
                String redisMatchKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, "*");
                List<String> list = new ArrayList<>();
                Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(redisMatchKey).count(limit).build());
                while (cursor.hasNext()) {
                    byte[] bytes = cursor.next();
                    String key = serializer.deserialize(bytes);
                    list.add(key);
                }
                return list;
            }
        });
        List<TccTransactionInfo> list = redisKeyList.stream().map(new Function<String, String>() {
            @Override
            public String apply(String redisKey) {
                String tid = parseTid(redisKey);
                return tid;
            }
        }).map(new Function<String, TccTransactionInfo>() {
            @Override
            public TccTransactionInfo apply(String tid) {
                TccTransactionInfo info = getTccTransactionInfo(tid);
                if (null == info) {
                    deleteTccTransaction(tid);
                    return null;
                }
                return info;
            }
        }).filter(new Predicate<TccTransactionInfo>() {
            @Override
            public boolean test(TccTransactionInfo tccTransactionInfo) {
                if (null != tccTransactionInfo) {
                    return true;
                }
                return false;
            }
        }).collect(Collectors.toList());
        return list;
    }

    private TccTransactionInfo getTccTransactionInfo(String queryTid) {
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, queryTid);
        Map<String, String> map = forHash.entries(redisKey);
        String tid = map.get(TID_KEY);
        if (null == tid) {
            return null;
        }
        String timeoutString = map.get(TIMEOUT_KEY);
        long timeout = NumberUtils.toLong(timeoutString, 0);
        String createTimeString = map.get(CREATE_TIME_KEY);
        long createTime = NumberUtils.toLong(createTimeString, 0);
        String tccStatus = map.get(STATUS_KEY);
        String timeoutMillisString = map.get(TIMEOUT_MILLIS_KEY);
        long timeoutMillis = NumberUtils.toLong(timeoutMillisString, 0);
        String compensateTimeoutMillisString = map.get(COMPENSATE_TIMEOUT_MILLIS_KEY);
        long compensateTimeoutMillis = NumberUtils.toLong(compensateTimeoutMillisString, 0);
        TccTransactionInfo info = new TccTransactionInfo();
        info.setTid(tid);
        info.setTimeout(timeout);
        info.setCreateTime(createTime);
        info.setTccStatus(tccStatus);
        info.setTccTimeoutMillis(timeoutMillis);
        info.setCompensateTimeoutMillis(compensateTimeoutMillis);
        return info;
    }

    private String parseTid(String redisKey) {
        String[] splits = redisKey.split(":");
        String tid = splits[splits.length - 1];
        return tid;
    }

    @Override
    public void updateTccTransactionStatus(String tid, TccConstants.TccStatus tccStatus) {
        HashOperations<String, String, String> forHash = redisTemplate.opsForHash();
        String redisKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, tid);
        Map<String, String> map = new HashMap<>();
        map.put(STATUS_KEY, tccStatus.toString());
        long timeout = System.currentTimeMillis() + config.getCompensateTimeout();
        map.put(COMPENSATE_TIMEOUT_MILLIS_KEY, String.valueOf(timeout));
        forHash.putAll(redisKey, map);
    }

    @Override
    public void deleteTccTransaction(String tid) {
        String transactionKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), TRANSACTION_KEY, tid);
        String methodKey = RedisKeyUtil.getRedisKey(config.getNameSpace(), METHODS_KEY, tid);
        redisTemplate.delete(Arrays.asList(transactionKey, methodKey));
    }
}
