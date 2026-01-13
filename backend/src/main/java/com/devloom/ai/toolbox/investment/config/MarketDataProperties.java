package com.devloom.ai.toolbox.investment.config;

import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 市场数据配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "investment.market-data")
public class MarketDataProperties {

    /**
     * 数据源配置 (按市场类型分组)
     * <p>
     * 例如:
     * data-sources:
     *   a-share:
     *     - name: akshare
     *       priority: 1
     *   us:
     *     - name: yfinance
     *       priority: 1
     */
    private Map<String, List<DataSourceConfig>> dataSources = new HashMap<>();

    /**
     * 缓存配置
     */
    private CacheSettings cache = new CacheSettings();

    /**
     * 熔断器配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();

    /**
     * 健康检查配置
     */
    private HealthCheckConfig healthCheck = new HealthCheckConfig();

    /**
     * 缓存配置
     */
    @Data
    public static class CacheSettings {
        /**
         * L1本地缓存配置
         */
        private CacheConfig l1 = new CacheConfig();

        /**
         * L2 Redis缓存配置
         */
        private CacheConfig l2 = new CacheConfig();
    }

    /**
     * 健康检查配置
     */
    @Data
    public static class HealthCheckConfig {
        /**
         * 是否启用健康检查
         */
        private Boolean enabled = true;

        /**
         * 检查间隔 (秒)
         */
        private Integer intervalSeconds = 60;

        /**
         * 健康检查超时 (秒)
         */
        private Integer timeoutSeconds = 5;

        /**
         * 成功率阈值 (低于此值认为不健康)
         */
        private Double successRateThreshold = 0.5;
    }

    /**
     * 获取指定市场的数据源配置列表
     */
    public List<DataSourceConfig> getDataSourcesForMarket(MarketType market) {
        String key = market.getCode().toLowerCase().replace("_", "-");
        return dataSources.getOrDefault(key, List.of());
    }

    /**
     * 获取所有已启用的数据源配置
     */
    public List<DataSourceConfig> getAllEnabledDataSources() {
        return dataSources.values().stream()
            .flatMap(List::stream)
            .filter(config -> config.getEnabled() != null && config.getEnabled())
            .toList();
    }
}
