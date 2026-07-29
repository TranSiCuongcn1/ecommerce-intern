package com.trancuong.ecommerce.common.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimitService rateLimitService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @Mock
    private RateLimit rateLimit;

    @InjectMocks
    private RateLimitAspect rateLimitAspect;

    @Test
    void enforceRateLimit_whenAllowed_proceedsExecution() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("login");
        when(rateLimit.keyPrefix()).thenReturn("rate_limit:");
        when(rateLimit.capacity()).thenReturn(5);
        when(rateLimit.durationSeconds()).thenReturn(60);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt())).thenReturn(true);
        when(joinPoint.proceed()).thenReturn("SUCCESS");

        Object result = rateLimitAspect.enforceRateLimit(joinPoint, rateLimit);

        assertThat(result).isEqualTo("SUCCESS");
    }

    @Test
    void enforceRateLimit_whenExceeded_throwsRateLimitExceededException() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("login");
        when(rateLimit.keyPrefix()).thenReturn("rate_limit:");
        when(rateLimit.capacity()).thenReturn(5);
        when(rateLimit.durationSeconds()).thenReturn(60);
        when(rateLimitService.tryConsume(anyString(), anyInt(), anyInt())).thenReturn(false);

        assertThatThrownBy(() -> rateLimitAspect.enforceRateLimit(joinPoint, rateLimit))
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Too many requests");
    }
}
