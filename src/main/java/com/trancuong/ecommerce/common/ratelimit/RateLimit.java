package com.trancuong.ecommerce.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int capacity() default 5;
    int durationSeconds() default 60;
    String keyPrefix() default "rate_limit:";
}
