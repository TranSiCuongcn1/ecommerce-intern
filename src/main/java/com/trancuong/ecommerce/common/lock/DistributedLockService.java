package com.trancuong.ecommerce.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistributedLockService {

    private final RedissonClient redissonClient;

    public <T> T executeWithLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean isAcquired = false;
        try {
            log.debug("Attempting to acquire distributed lock: {}", lockKey);
            isAcquired = lock.tryLock(waitTimeSeconds, leaseTimeSeconds, TimeUnit.SECONDS);
            if (!isAcquired) {
                log.warn("Failed to acquire distributed lock: {}", lockKey);
                throw new LockAcquisitionException("Could not acquire lock for key: " + lockKey + ". Please try again.");
            }
            log.debug("Successfully acquired distributed lock: {}", lockKey);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("Thread interrupted while waiting for lock: " + lockKey);
        } finally {
            if (isAcquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("Released distributed lock: {}", lockKey);
            }
        }
    }

    public void executeWithLock(String lockKey, long waitTimeSeconds, long leaseTimeSeconds, Runnable runnable) {
        executeWithLock(lockKey, waitTimeSeconds, leaseTimeSeconds, () -> {
            runnable.run();
            return null;
        });
    }
}
