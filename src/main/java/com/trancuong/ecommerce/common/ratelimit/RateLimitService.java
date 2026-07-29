package com.trancuong.ecommerce.common.ratelimit;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedissonClient redissonClient;

    public boolean tryConsume(String key, int capacity, int durationSeconds) {
        String redisKey = "ratelimit:" + key;
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(redisKey);
        if (!rateLimiter.isExists()) {
            rateLimiter.trySetRate(RateType.OVERALL, capacity, durationSeconds, RateIntervalUnit.SECONDS);
            rateLimiter.expire(Duration.ofSeconds(durationSeconds * 2L));
        }
        return rateLimiter.tryAcquire(1);
    }
}
