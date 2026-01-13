package com.devloom.ai.toolbox.investment.infra.health;

import com.devloom.ai.toolbox.investment.config.MarketDataProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据源健康检查器
 * <p>
 * 负责监控数据源的健康状态,支持熔断和自动恢复
 */
@Slf4j
@Component
public class DataSourceHealthChecker {

    private final MarketDataProperties properties;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * 熔断器映射
     */
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    /**
     * 成功请求计数
     */
    private final Map<String, AtomicInteger> successCounters = new ConcurrentHashMap<>();

    /**
     * 失败请求计数
     */
    private final Map<String, AtomicInteger> failureCounters = new ConcurrentHashMap<>();

    /**
     * 最后成功时间
     */
    private final Map<String, LocalDateTime> lastSuccessTime = new ConcurrentHashMap<>();

    /**
     * 最后检查时间
     */
    private final Map<String, LocalDateTime> lastCheckTime = new ConcurrentHashMap<>();

    /**
     * 健康状态缓存 (避免频繁检查)
     */
    private final Map<String, LocalDateTime> healthCache = new ConcurrentHashMap<>();

    /**
     * 缓存有效期 (秒)
     */
    private static final int HEALTH_CACHE_TTL_SECONDS = 30;

    public DataSourceHealthChecker(MarketDataProperties properties) {
        this.properties = properties;
        this.circuitBreakerRegistry = createCircuitBreakerRegistry();
    }

    /**
     * 创建熔断器注册表
     */
    private CircuitBreakerRegistry createCircuitBreakerRegistry() {
        var config = properties.getCircuitBreaker();

        CircuitBreakerConfig cbConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(config.getFailureRateThreshold())
            .waitDurationInOpenState(Duration.ofSeconds(config.getWaitDurationInOpenStateSeconds()))
            .slidingWindowSize(config.getSlidingWindowSize())
            .permittedNumberOfCallsInHalfOpenState(config.getPermittedNumberOfCallsInHalfOpenState())
            .minimumNumberOfCalls(config.getMinimumNumberOfCalls())
            .automaticTransitionFromOpenToHalfOpenEnabled(
                config.getAutomaticTransitionFromOpenToHalfOpenEnabled())
            .build();

        return CircuitBreakerRegistry.of(cbConfig);
    }

    /**
     * 获取或创建熔断器
     */
    public CircuitBreaker getOrCreateCircuitBreaker(String dataSourceName) {
        return circuitBreakers.computeIfAbsent(dataSourceName, name -> {
            log.info("Creating circuit breaker for: {}", name);
            return circuitBreakerRegistry.circuitBreaker(name);
        });
    }

    /**
     * 检查数据源是否健康
     *
     * @param dataSourceName 数据源名称
     * @return true-健康, false-不健康
     */
    public boolean isHealthy(String dataSourceName) {
        // 1. 检查熔断器状态
        CircuitBreaker cb = getOrCreateCircuitBreaker(dataSourceName);
        if (cb.getState() == CircuitBreaker.State.OPEN) {
            log.debug("Circuit breaker is OPEN for: {}", dataSourceName);
            return false;
        }

        // 2. 检查健康缓存
        LocalDateTime cached = healthCache.get(dataSourceName);
        if (cached != null) {
            Duration age = Duration.between(cached, LocalDateTime.now());
            if (age.toSeconds() < HEALTH_CACHE_TTL_SECONDS) {
                return true;
            }
        }

        // 3. 检查最近的成功率
        if (!isRecentlySuccessful(dataSourceName)) {
            log.warn("Data source {} has not had successful requests recently", dataSourceName);
            return false;
        }

        return true;
    }

    /**
     * 检查数据源最近是否有成功请求
     */
    public boolean isRecentlySuccessful(String dataSourceName) {
        LocalDateTime lastSuccess = lastSuccessTime.get(dataSourceName);
        if (lastSuccess == null) {
            // 没有请求记录,认为是新的,返回true
            return true;
        }

        // 5分钟内有过成功请求
        Duration sinceLastSuccess = Duration.between(lastSuccess, LocalDateTime.now());
        return sinceLastSuccess.toMinutes() < 5;
    }

    /**
     * 记录请求成功
     */
    public void recordSuccess(String dataSourceName) {
        successCounters.computeIfAbsent(dataSourceName, k -> new AtomicInteger(0))
            .incrementAndGet();
        lastSuccessTime.put(dataSourceName, LocalDateTime.now());

        // 清除熔断器半开状态
        CircuitBreaker cb = circuitBreakers.get(dataSourceName);
        if (cb != null && cb.getState() == CircuitBreaker.State.HALF_OPEN) {
            cb.transitionToClosedState();
            log.info("Circuit breaker for {} recovered from HALF_OPEN to CLOSED", dataSourceName);
        }
    }

    /**
     * 记录请求失败
     */
    public void recordFailure(String dataSourceName) {
        failureCounters.computeIfAbsent(dataSourceName, k -> new AtomicInteger(0))
            .incrementAndGet();

        // 更新熔断器
        CircuitBreaker cb = getOrCreateCircuitBreaker(dataSourceName);
        cb.onError(0, new RuntimeException("Request failed"));
    }

    /**
     * 获取成功率
     *
     * @param dataSourceName 数据源名称
     * @param duration       时间窗口
     * @return 成功率 (0.0 - 1.0)
     */
    public double getSuccessRate(String dataSourceName, Duration duration) {
        AtomicInteger success = successCounters.get(dataSourceName);
        AtomicInteger failure = failureCounters.get(dataSourceName);

        int successCount = success != null ? success.get() : 0;
        int failureCount = failure != null ? failure.get() : 0;

        int total = successCount + failureCount;
        if (total == 0) {
            return 1.0; // 没有请求,默认成功
        }

        return (double) successCount / total;
    }

    /**
     * 获取健康信息
     */
    public HealthInfo getHealthInfo(String dataSourceName) {
        CircuitBreaker cb = circuitBreakers.get(dataSourceName);
        CircuitBreaker.State state = cb != null
            ? cb.getState()
            : CircuitBreaker.State.CLOSED;

        LocalDateTime lastSuccess = lastSuccessTime.get(dataSourceName);
        double successRate = getSuccessRate(dataSourceName, Duration.ofMinutes(1));

        return new HealthInfo(
            dataSourceName,
            state.name(),
            successRate,
            lastSuccess,
            getSuccessCount(dataSourceName),
            getFailureCount(dataSourceName)
        );
    }

    /**
     * 获取成功请求数
     */
    public int getSuccessCount(String dataSourceName) {
        AtomicInteger counter = successCounters.get(dataSourceName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 获取失败请求数
     */
    public int getFailureCount(String dataSourceName) {
        AtomicInteger counter = failureCounters.get(dataSourceName);
        return counter != null ? counter.get() : 0;
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics(String dataSourceName) {
        successCounters.remove(dataSourceName);
        failureCounters.remove(dataSourceName);
        lastSuccessTime.remove(dataSourceName);
        lastCheckTime.remove(dataSourceName);
        healthCache.remove(dataSourceName);

        CircuitBreaker cb = circuitBreakers.get(dataSourceName);
        if (cb != null) {
            cb.reset();
        }

        log.info("Reset statistics for data source: {}", dataSourceName);
    }

    /**
     * 主动健康检查 (定时任务)
     */
    @Scheduled(fixedRateString = "${investment.market-data.health-check.interval-seconds:60}000")
    public void performHealthCheck() {
        if (!properties.getHealthCheck().getEnabled()) {
            return;
        }

        log.debug("Performing scheduled health check");

        circuitBreakers.forEach((name, cb) -> {
            try {
                // 检查熔断器状态
                if (cb.getState() == CircuitBreaker.State.OPEN) {
                    // 尝试半开状态
                    log.info("Trying to transition circuit breaker for {} to HALF_OPEN", name);
                    cb.transitionToHalfOpenState();
                }
            } catch (Exception e) {
                log.error("Error during health check for: {}", name, e);
            }
        });
    }

    /**
     * 清理过期的统计信息 (每小时执行)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredStatistics() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(1);

        successCounters.entrySet().removeIf(entry -> {
            LocalDateTime lastSuccess = lastSuccessTime.get(entry.getKey());
            return lastSuccess != null && lastSuccess.isBefore(threshold);
        });

        failureCounters.entrySet().removeIf(entry -> {
            // 失败记录保留更短时间
            return true; // 简单处理,直接清空
        });

        log.info("Cleaned up expired statistics");
    }

    /**
     * 健康信息记录
     */
    @Data
    public static class HealthInfo {
        private final String dataSourceName;
        private final String circuitBreakerState;
        private final double successRate;
        private final LocalDateTime lastSuccessTime;
        private final int successCount;
        private final int failureCount;
    }
}
