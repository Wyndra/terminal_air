package top.srcandy.candyterminal.service;

import java.util.concurrent.TimeUnit;

/**
 * RedisService 提供 Redis 相关的操作接口。
 * 该接口包含基本的 Redis 操作，如字符串、哈希、列表、集合、有序集合的增删改查。
 *
 * @author wyndra
 * @version 1.0
 */
public interface RedisService {

    /**
     * 设置键值对。
     *
     * @param key   Redis 键
     * @param value Redis 值
     */
    void set(String key, String value);

    /**
     * 设置带有过期时间的键值对。
     *
     * @param key     Redis 键
     * @param value   Redis 值
     * @param timeout 过期时间（秒）
     */
    void set(String key, String value, long timeout);

    /**
     * 设置带有过期时间和时间单位的键值对。
     *
     * @param key     Redis 键
     * @param value   Redis 值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 获取指定键的值。
     *
     * @param key Redis 键
     * @return Redis 值，如果键不存在返回 null
     */
    String get(String key);

    /**
     * 删除指定的键。
     *
     * @param key Redis 键
     */
    void delete(String key);

    /**
     * 设置键的过期时间。
     *
     * @param key     Redis 键
     * @param timeout 过期时间（秒）
     * @return 是否设置成功
     */
    boolean expire(String key, long timeout);

    /**
     * 设置键的过期时间。
     *
     * @param key     Redis 键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 是否设置成功
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 判断键是否存在。
     *
     * @param key Redis 键
     * @return 如果存在返回 true，否则返回 false
     */
    boolean hasKey(String key);

    /**
     * 递增键的值。
     *
     * @param key   Redis 键
     * @param delta 增加的值
     * @return 递增后的值
     */
    long increment(String key, long delta);

    /**
     * 递减键的值。
     *
     * @param key   Redis 键
     * @param delta 递减的值
     * @return 递减后的值
     */
    long decrement(String key, long delta);

    /**
     * 设置哈希表中的字段值。
     *
     * @param key     Redis 键
     * @param hashKey 哈希字段
     * @param value   哈希值
     */
    void hSet(String key, String hashKey, String value);

    /**
     * 获取哈希表中的字段值。
     *
     * @param key     Redis 键
     * @param hashKey 哈希字段
     * @return 哈希字段对应的值
     */
    String hGet(String key, String hashKey);

    /**
     * 向列表右侧添加元素。
     *
     * @param key   Redis 键
     * @param value 列表值
     */
    void rPush(String key, String value);

    /**
     * 从列表左侧弹出元素。
     *
     * @param key Redis 键
     * @return 列表中的第一个元素
     */
    String lPop(String key);

    /**
     * 向集合添加元素。
     *
     * @param key    Redis 键
     * @param values 集合中的值
     */
    void sAdd(String key, String... values);

    /**
     * 判断值是否存在于集合中。
     *
     * @param key   Redis 键
     * @param value 需要检查的值
     * @return 如果值存在返回 true，否则返回 false
     */
    boolean sIsMember(String key, String value);

    /**
     * 向有序集合添加元素。
     *
     * @param key   Redis 键
     * @param value 值
     * @param score 分数（排序依据）
     */
    void zAdd(String key, String value, double score);

    /**
     * 获取有序集合中某个值的排名（从 0 开始）。
     *
     * @param key   Redis 键
     * @param value 值
     * @return 排名（索引），如果值不存在返回 null
     */
    Long zRank(String key, String value);
}
