package com.devloom.ai.toolbox.investment.common.infra.marketdata.config;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.router.MarketDataFeature;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * 数据源路由配置。
 *
 * <p>支持按功能配置数据源和故障转移链。</p>
 *
 * @author devloom
 */
@Data
@ConfigurationProperties(prefix = "investment.marketdata")
public class MarketDataProperties {

    /** 数据源配置。 */
    private Map<String, DataSourceConfig> sources;

    /** 功能路由配置。 */
    private Map<String, FeatureRouting> routing;

    @Data
    public static class DataSourceConfig {
        /** 是否启用。 */
        private boolean enabled = true;
    }

    @Data
    public static class FeatureRouting {
        /** 主数据源名称。 */
        private String primary;

        /** 故障转移链。 */
        private List<String> fallback;
    }

    /**
     * 获取功能的路由配置。
     */
    public FeatureRouting getRouting(MarketDataFeature feature) {
        if (routing == null) {
            return null;
        }
        return routing.get(feature.getKey());
    }

    /**
     * 获取数据源配置。
     */
    public DataSourceConfig getSourceConfig(String name) {
        if (sources == null) {
            return null;
        }
        return sources.get(name);
    }
}
