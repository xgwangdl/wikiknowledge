package com.wikiknowledge.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;/** 刷新令牌 Redis 存储与校验 */


@Service
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String username, String token, Duration ttl) {
        redisTemplate.opsForValue().set(key(username), token, ttl);
    }

    public boolean matches(String username, String token) {
        String stored = redisTemplate.opsForValue().get(key(username));
        return token.equals(stored);
    }

    public void delete(String username) {
        redisTemplate.delete(key(username));
    }

    private String key(String username) {
        return KEY_PREFIX + username;
    }
}
