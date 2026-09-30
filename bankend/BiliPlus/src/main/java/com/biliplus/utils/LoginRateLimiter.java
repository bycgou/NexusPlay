package com.biliplus.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 基于 Redis 的登录/发信限速
 */
@Component
public class LoginRateLimiter {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /** 同一 key 在 window 内最多 maxAttempts 次 */
    public boolean allow(String key, int maxAttempts, Duration window) {
        String redisKey = "rl:" + key;
        Long n = stringRedisTemplate.opsForValue().increment(redisKey);
        if (n != null && n == 1L) {
            stringRedisTemplate.expire(redisKey, window);
        }
        return n == null || n <= maxAttempts;
    }

    public void reset(String key) {
        stringRedisTemplate.delete("rl:" + key);
    }

    /** 读取当前计数（不自增） */
    public long count(String key) {
        String v = stringRedisTemplate.opsForValue().get("rl:" + key);
        if (v == null) {
            return 0L;
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /** 登录类 IP 限流：默认 20 次 / 15 分钟（覆盖多账号喷洒） */
    public boolean allowLoginIp(String ip) {
        return allow("login-ip:" + ip, 20, Duration.ofMinutes(15));
    }

    /** 单账号登录失败限流：5 次 / 15 分钟 */
    public boolean allowAccountLogin(String accountKey) {
        return allow(accountKey, 5, Duration.ofMinutes(15));
    }
}
