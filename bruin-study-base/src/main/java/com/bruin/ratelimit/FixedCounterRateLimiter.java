package com.bruin.ratelimiter;

/**
 * @author xiongwenwen
 * @since 2022/7/20 2:16 下午
 */
public class FixedCounterRateLimiter {

    // 窗口大小，毫秒为单位
    private long windowSize;
    // 窗口内限流大小
    private int limit;
    // 当前窗口的计数器
    private int count;

    private long timestamp = System.currentTimeMillis();

    private FixedCounterRateLimiter(long windowSize, int limit) {
        this.windowSize = windowSize;
        this.limit = limit;
    }

    public boolean tryAcquire() {
        long now = System.currentTimeMillis();
        // 窗口内请求数量小雨繁殖，更新计数放行
        if (now - timestamp < windowSize) {
            if (count < limit) {
                count ++;
                return true;
            } else {
                return false;
            }
        }

        // 时间窗口过期，重置计数器和时间戳
        count = 0;
        timestamp = now;
        return true;
    }
}