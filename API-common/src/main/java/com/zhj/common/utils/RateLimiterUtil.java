package com.zhj.common.utils;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterUtil {
    private static final Integer PERMITS_PER_SECOND = 1; // 每秒允许的请求数

    private static final RateLimiter rateLimiter = RateLimiter.create(PERMITS_PER_SECOND);

    public static boolean acquire() {
        return rateLimiter.tryAcquire();
    }
}