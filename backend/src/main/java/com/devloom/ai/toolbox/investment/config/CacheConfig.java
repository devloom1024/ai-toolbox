package com.devloom.ai.toolbox.investment.config;

import lombok.Data;

import java.time.Duration;

/**
 * 缓存配置
 */
@Data
public class CacheConfig {
    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 最大容量
     */
    private Integer maxSize = 10000;

    /**
     * 写入后过期时间 (秒)
     */
    private Long expireAfterWriteSeconds = 60L;

    /**
     * 访问后过期时间 (秒)
     */
    private Long expireAfterAccessSeconds;

    /**
     * 初始容量
     */
    private Integer initialCapacity = 100;

    /**
     * 是否记录统计信息
     */
    private Boolean recordStats = true;

    /**
     * 获取过期时间 Duration
     */
    public Duration getExpireAfterWriteDuration() {
        return Duration.ofSeconds(expireAfterWriteSeconds);
    }

    /**
     * 获取访问后过期 Duration
     */
    public Duration getExpireAfterAccessDuration() {
        return expireAfterAccessSeconds != null
            ? Duration.ofSeconds(expireAfterAccessSeconds)
            : null;
    }
}
