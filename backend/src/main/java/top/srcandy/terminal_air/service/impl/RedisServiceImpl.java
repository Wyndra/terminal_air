package top.srcandy.terminal_air.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.srcandy.terminal_air.service.RedisService;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // ------- String 类型 -------

    @Override
    public void set(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, timeout, unit));
    }

    @Override
    public long increment(String key, long delta) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().increment(key, delta)).orElse(0L);
    }

    @Override
    public long decrement(String key, long delta) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().decrement(key, delta)).orElse(0L);
    }

    // ------- Hash 类型 -------

    @Override
    public void hSet(String key, String hashKey, String value) {
        stringRedisTemplate.opsForHash().put(key, hashKey, value);
    }

    @Override
    public String hGet(String key, String hashKey) {
        Object result = stringRedisTemplate.opsForHash().get(key, hashKey);
        return result != null ? result.toString() : null;
    }

    // ------- List 类型 -------

    @Override
    public void rPush(String key, String value) {
        stringRedisTemplate.opsForList().rightPush(key, value);
    }

    @Override
    public String lPop(String key) {
        return stringRedisTemplate.opsForList().leftPop(key);
    }

    // ------- Set 类型 -------

    @Override
    public void sAdd(String key, String... values) {
        stringRedisTemplate.opsForSet().add(key, values);
    }

    @Override
    public boolean sIsMember(String key, String value) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(key, value));
    }

    // ------- ZSet 类型 -------

    @Override
    public void zAdd(String key, String value, double score) {
        stringRedisTemplate.opsForZSet().add(key, value, score);
    }

    @Override
    public Long zRank(String key, String value) {
        return stringRedisTemplate.opsForZSet().rank(key, value);
    }

    // ------- Object 类型支持（通用） -------

    public void setObject(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }



    public <T> T getObject(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }

        // 若 RedisTemplate 的序列化器不是 JSON，还需手动转 JSON -> Java 对象
        if (value instanceof String) {
            try {
                return (T) objectMapper.readValue((String) value, clazz);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Redis 中对象反序列化失败", e);
            }
        }

        // 已是对象直接返回（前提是存入时就是这个类型）
        if (clazz.isInstance(value)) {
            return (T) clazz.cast(value);
        }

        throw new IllegalStateException("Redis 返回的对象类型不匹配，实际类型: " + value.getClass());
    }

    public <T> T execute(RedisCallback<T> callback) {
        // 获取 Redis 连接对象
        RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

        try {
            // 执行回调，传入连接对象
            return callback.doInRedis(connection);
        } finally {
            connection.close();
        }
    }

    @Override
    public void setObject(String key, Object value) {
        // 直接调用带过期时间的方法，传入0表示永不过期
        setObject(key, value, 0, TimeUnit.SECONDS);
    }
}
