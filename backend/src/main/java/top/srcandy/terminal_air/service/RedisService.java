package top.srcandy.terminal_air.service;

import org.springframework.data.redis.core.RedisCallback;

import java.util.concurrent.TimeUnit;


public interface RedisService {

    // -------- String 操作 --------
    void set(String key, String value);

    void set(String key, String value, long timeout, TimeUnit unit);

    String get(String key);

    void delete(String key);

    boolean hasKey(String key);

    boolean expire(String key, long timeout, TimeUnit unit);

    long increment(String key, long delta);

    long decrement(String key, long delta);

    // -------- Hash 操作 --------
    void hSet(String key, String hashKey, String value);

    String hGet(String key, String hashKey);

    // -------- List 操作 --------
    void rPush(String key, String value);

    String lPop(String key);

    // -------- Set 操作 --------
    void sAdd(String key, String... values);

    boolean sIsMember(String key, String value);

    // -------- ZSet 操作 --------
    void zAdd(String key, String value, double score);

    Long zRank(String key, String value);

    // -------- Object 操作（需使用 RedisTemplate<Object>）--------
    void setObject(String key, Object value, long timeout, TimeUnit unit);

    Object getObject(String key);

    <T> T getObject(String key, Class<T> clazz);

    <T> T execute(RedisCallback<T> callback);

    /**
     * 存储对象到Redis（支持嵌套子对象，无过期时间）
     * @param key 键
     * @param value 值（可以是包含子对象的复杂对象）
     */
    void setObject(String key, Object value);
}

