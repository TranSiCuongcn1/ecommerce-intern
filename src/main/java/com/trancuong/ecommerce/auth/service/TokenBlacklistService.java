package com.trancuong.ecommerce.auth.service;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "jwt:blacklist:";
    private static final String BLACKLISTED_VALUE = "revoked";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String tokenType, String tokenId, Duration ttl) {
        if (tokenId == null || tokenId.isBlank() || ttl == null || !ttl.isPositive()) {
            return;
        }

        redisTemplate.opsForValue().set(buildKey(tokenType, tokenId), BLACKLISTED_VALUE, ttl);
    }

    public boolean isBlacklisted(String tokenType, String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }

        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(tokenType, tokenId)));
    }

    private String buildKey(String tokenType, String tokenId) {
        return KEY_PREFIX + tokenType + ":" + tokenId;
    }
}
