package com.x.file.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * 高并发适配的分布式锁服务类（基于 Redis 实现，支持可重入、重试、续期）
 *
 * @author whj
 */
@Slf4j
@Service
public class DistributedLockService {

    // ==================== 常量定义（避免硬编码，提升可维护性）====================
    /** 释放锁的 Lua 脚本（原子操作：判断+删除，避免误释放） */
    private static final String RELEASE_REENTRANT_LOCK_SCRIPT =
            "local lockKey = KEYS[1] " +
                    "local requestId = ARGV[1] " +
                    "local expireTime = tonumber(ARGV[2]) " +
                    "local lockValue = redis.call('get', lockKey) " +
                    "if not lockValue then " +
                    "    return 1 " +  // 锁不存在，视为释放成功
                    "end " +
                    // 前缀匹配：判断锁值是否以 requestId: 开头（兼容可重入格式）
                    "if string.sub(lockValue, 1, string.len(requestId) + 1) ~= requestId .. ':' then " +
                    "    return 0 " +  // 锁不属于当前请求，拒绝释放
                    "end " +
                    //解析重入次数
                    "local count = tonumber(string.sub(lockValue, string.len(requestId) + 2)) " +
                            "if count > 1 then " +
                            //重入次数>1：递减计数并续期
                    "    redis.call('setex', lockKey, expireTime, requestId .. ':' .. (count - 1)) " +
                            "else " +
                            //重入次数=1：直接删除锁
                    "    redis.call('del', lockKey) " +
                            "end " +
                            "return 1";  // 释放成功

    /** 获取可重入锁的 Lua 脚本（原子操作：判断+设置，支持重入计数） */
    private static final String ACQUIRE_REENTRANT_LOCK_SCRIPT =
            "local lockKey = KEYS[1] " +
                    "local requestId = ARGV[1] " +
                    "local expireTime = tonumber(ARGV[2]) " +
                    "local currentValue = redis.call('get', lockKey) " +
                    "if not currentValue then " +
                    "    redis.call('setex', lockKey, expireTime, requestId .. ':1') " +
                    "    return 1 " +  // 锁不存在，创建锁（重入次数=1）
                    "end " +
                    "if string.find(currentValue, requestId .. ':') == 1 then " +
                    "    local count = tonumber(string.sub(currentValue, string.len(requestId) + 2)) " +
                    "    redis.call('setex', lockKey, expireTime, requestId .. ':' .. (count + 1)) " +
                    "    return 1 " +  // 锁属于当前请求，重入次数+1，续期
                    "end " +
                    "return 0";  // 锁被其他请求持有，获取失败

    /** 默认锁过期时间（30秒，避免死锁） */
    private static final long DEFAULT_EXPIRE_SECONDS = 30;
    /** 默认重试次数（0次，不重试） */
    private static final int DEFAULT_RETRY_TIMES = 0;
    /** 默认重试间隔（100毫秒） */
    private static final long DEFAULT_RETRY_INTERVAL_MS = 100;

    // ==================== 依赖注入 ====================
    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> releaseLockScript;
    private final DefaultRedisScript<Long> acquireReentrantLockScript;

    /**
     * 构造方法：初始化 Redis 脚本和序列化器
     * 优先使用 StringRedisTemplate（避免序列化问题）
     */
    @Autowired
    public DistributedLockService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        // 强制设置 Key/Value 序列化器为 String 类型（关键：避免二进制序列化导致对比失败）
        this.stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        this.stringRedisTemplate.setValueSerializer(new StringRedisSerializer());

        // 初始化释放锁脚本
        this.releaseLockScript = new DefaultRedisScript<>(RELEASE_REENTRANT_LOCK_SCRIPT, Long.class);
        // 初始化可重入锁脚本
        this.acquireReentrantLockScript = new DefaultRedisScript<>(ACQUIRE_REENTRANT_LOCK_SCRIPT, Long.class);
    }

    // ==================== 核心方法 ====================

    /**
     * 尝试获取分布式锁（可重入，支持重试）
     *
     * @param lockKey     锁的唯一标识（如：file:upload:md5值）
     * @param requestId   请求标识（建议用 UUID+线程ID，确保唯一性）
     * @param expireTime  锁过期时间（秒）
     * @param retryTimes  重试次数（0=不重试）
     * @param retryIntervalMs 重试间隔（毫秒）
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime, int retryTimes, long retryIntervalMs) {
        // 参数校验（避免无效请求）
        Assert.hasText(lockKey, "锁键（lockKey）不能为空");
        Assert.hasText(requestId, "请求标识（requestId）不能为空");
        Assert.isTrue(expireTime > 0, "锁过期时间必须大于0秒");
        Assert.isTrue(retryTimes >= 0, "重试次数不能为负数");
        Assert.isTrue(retryIntervalMs >= 0, "重试间隔不能为负数");

        long currentRetry = 0;
        do {
            // 执行 Lua 脚本获取锁（原子操作）
            Long result = stringRedisTemplate.execute(
                    acquireReentrantLockScript,
                    Collections.singletonList(lockKey),
                    requestId,
                    String.valueOf(expireTime)
            );

            // 结果判断（1=成功，0=失败，null=异常）
            if (result == 1) {
                log.info("获取分布式锁成功：lockKey={}, requestId={}, 剩余重试次数={}", lockKey, requestId, retryTimes - currentRetry);
                return true;
            }

            // 重试逻辑（未达到重试次数时休眠）
            if (currentRetry < retryTimes) {
                try {
                    Thread.sleep(retryIntervalMs);
                    log.debug("获取锁失败，进行重试：lockKey={}, requestId={}, 重试次数={}/{}",
                            lockKey, requestId, currentRetry + 1, retryTimes);
                } catch (InterruptedException e) {
                    log.error("获取锁重试时线程被中断：lockKey={}, requestId={}", lockKey, requestId, e);
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    return false;
                }
            }
            currentRetry++;
        } while (currentRetry <= retryTimes);

        log.warn("获取分布式锁失败（已达最大重试次数）：lockKey={}, requestId={}, 重试次数={}",
                lockKey, requestId, retryTimes);
        return false;
    }

    /**
     * 重载：使用默认重试配置获取可重入锁
     */
    public boolean tryLock(String lockKey, String requestId, long expireTime) {
        return tryLock(lockKey, requestId, expireTime, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL_MS);
    }

    /**
     * 重载：使用默认过期时间和重试配置获取可重入锁
     */
    public boolean tryLock(String lockKey, String requestId) {
        return tryLock(lockKey, requestId, DEFAULT_EXPIRE_SECONDS, DEFAULT_RETRY_TIMES, DEFAULT_RETRY_INTERVAL_MS);
    }

    /**
     * 释放分布式可重入锁（原子操作，支持计数递减）
     *
     * @param lockKey   锁的唯一标识
     * @param requestId 请求标识（必须与获取锁时一致）
     * @return 是否释放成功
     */
    public boolean releaseLock(String lockKey, String requestId) {
        return releaseLock(lockKey, requestId, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 重载：支持自定义续期时间（用于重入次数递减时续期）
     */
    public boolean releaseLock(String lockKey, String requestId, long expireTime) {
        // 参数校验
        Assert.hasText(lockKey, "锁键（lockKey）不能为空");
        Assert.hasText(requestId, "请求标识（requestId）不能为空");
        Assert.isTrue(expireTime > 0, "锁过期时间必须大于0秒");

        try {
            // 执行 Lua 脚本释放锁（传递 requestId + 过期时间）
            Long result = stringRedisTemplate.execute(
                    releaseLockScript,
                    Collections.singletonList(lockKey),
                    requestId,
                    String.valueOf(expireTime)  // 传递过期时间，用于重入计数递减时续期
            );

            // 结果判断（1=成功，0=失败，null=异常）
            boolean success = result >= 1;
            if (success) {
                log.info("释放分布式锁成功：lockKey={}, requestId={}", lockKey, requestId);
            } else {
                log.warn("释放分布式锁失败（锁不存在或不属于当前请求）：lockKey={}, requestId={}", lockKey, requestId);
            }
            return success;
        } catch (Exception e) {
            log.error("释放分布式锁异常：lockKey={}, requestId={}", lockKey, requestId, e);
            return false;
        }
    }


    /**
     * 强制释放锁（慎用！仅用于特殊场景，如管理员清理过期锁）
     */
    public void forceReleaseLock(String lockKey) {
        Assert.hasText(lockKey, "锁键（lockKey）不能为空");
        try {
            stringRedisTemplate.delete(lockKey);
            log.warn("强制释放分布式锁：lockKey={}", lockKey);
        } catch (Exception e) {
            log.error("强制释放分布式锁异常：lockKey={}", lockKey, e);
        }
    }

    /**
     * 检查锁是否存在
     */
    public boolean isLockExists(String lockKey) {
        Assert.hasText(lockKey, "锁键（lockKey）不能为空");
        try {
            return stringRedisTemplate.hasKey(lockKey);
        } catch (Exception e) {
            log.error("检查锁是否存在异常：lockKey={}", lockKey, e);
            return false;
        }
    }
}