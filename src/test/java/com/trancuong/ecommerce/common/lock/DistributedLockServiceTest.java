package com.trancuong.ecommerce.common.lock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class DistributedLockServiceTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock rLock;

    @InjectMocks
    private DistributedLockService distributedLockService;

    @Test
    void executeWithLock_whenLockAcquired_executesSupplierAndReleasesLock() throws Exception {
        String lockKey = "lock:inventory:product:123";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);

        String result = distributedLockService.executeWithLock(lockKey, 5, 10, () -> "SUCCESS");

        assertThat(result).isEqualTo("SUCCESS");
        verify(rLock).unlock();
    }

    @Test
    void executeWithLock_whenLockNotAcquired_throwsLockAcquisitionException() throws Exception {
        String lockKey = "lock:inventory:product:123";
        when(redissonClient.getLock(lockKey)).thenReturn(rLock);
        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(false);

        assertThatThrownBy(() -> distributedLockService.executeWithLock(lockKey, 5, 10, () -> "SUCCESS"))
                .isInstanceOf(LockAcquisitionException.class)
                .hasMessageContaining("Could not acquire lock");
    }
}
