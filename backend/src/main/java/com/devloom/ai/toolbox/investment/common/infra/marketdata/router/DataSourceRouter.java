package com.devloom.ai.toolbox.investment.common.infra.marketdata.router;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.config.MarketDataProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据源路由器。
 *
 * <p>按功能类型选择数据源，支持配置化路由和故障转移。</p>
 *
 * @author claude
 */
@Slf4j
@Component
public class DataSourceRouter {

    /** 搜索功能路由键。 */
    public static final String FEATURE_SEARCH = "search";

    /** K 线功能路由键。 */
    public static final String FEATURE_KLINE = "kline";

    /** 基本面功能路由键。 */
    public static final String FEATURE_FUNDAMENTAL = "fundamental";

    /** 财务指标功能路由键。 */
    public static final String FEATURE_FINANCIAL = "financial";

    /** 资金流向功能路由键。 */
    public static final String FEATURE_CAPITAL_FLOW = "capitalFlow";

    private final List<MarketDataAdapter> adapters;
    private final MarketDataProperties properties;

    public DataSourceRouter(List<MarketDataAdapter> adapters, MarketDataProperties properties) {
        this.adapters = adapters;
        this.properties = properties;
        log.info("Initialized DataSourceRouter with {} adapters", adapters.size());
    }

    /**
     * 根据功能获取数据源适配器。
     *
     * @param feature 功能路由键
     * @param market 市场类型
     * @return 数据源适配器
     */
    public MarketDataAdapter select(String feature, String market) {
        MarketDataProperties.FeatureRouting routing = properties.getRouting(feature);

        if (routing == null) {
            // 默认使用第一个可用的适配器
            return selectFirstAvailable();
        }

        String primarySource = routing.getPrimary();

        // 1. 检查主数据源是否支持该市场
        if (isMarketSupported(primarySource, market)) {
            MarketDataAdapter adapter = findAdapter(primarySource);
            if (adapter != null && adapter.isAvailable()) {
                log.debug("Selected primary source: {} for feature: {}, market: {}",
                        primarySource, feature, market);
                return adapter;
            }
        }

        // 2. 故障转移
        return selectWithFallback(routing, feature, market);
    }

    /**
     * 获取搜索数据源适配器。
     */
    public MarketDataAdapter selectForSearch() {
        return select(FEATURE_SEARCH, null);
    }

    /**
     * 获取 K 线数据源适配器。
     */
    public MarketDataAdapter selectForKline(String market) {
        return select(FEATURE_KLINE, market);
    }

    /**
     * 获取基本面数据源适配器。
     */
    public MarketDataAdapter selectForFundamental(String market) {
        return select(FEATURE_FUNDAMENTAL, market);
    }

    /**
     * 获取财务指标数据源适配器。
     */
    public MarketDataAdapter selectForFinancial(String market) {
        return select(FEATURE_FINANCIAL, market);
    }

    /**
     * 获取资金流向数据源适配器。
     */
    public MarketDataAdapter selectForCapitalFlow(String market) {
        return select(FEATURE_CAPITAL_FLOW, market);
    }

    private MarketDataAdapter selectWithFallback(MarketDataProperties.FeatureRouting routing,
            String feature, String market) {
        List<String> chain = routing.getFallback();

        if (chain == null || chain.isEmpty()) {
            // 无故障转移链，回退到主数据源
            return selectPrimaryOrFirst(routing.getPrimary(), feature, market);
        }

        for (String sourceName : chain) {
            if (sourceName.equals(routing.getPrimary())) {
                continue;
            }

            if (!isMarketSupported(sourceName, market)) {
                continue;
            }

            MarketDataAdapter adapter = findAdapter(sourceName);
            if (adapter != null && adapter.isAvailable()) {
                log.info("Selected fallback source: {} for feature: {}, market: {}",
                        sourceName, feature, market);
                return adapter;
            }
        }

        // 回退到主数据源
        return selectPrimaryOrFirst(routing.getPrimary(), feature, market);
    }

    private MarketDataAdapter selectPrimaryOrFirst(String primarySource, String feature, String market) {
        MarketDataAdapter primary = findAdapter(primarySource);
        if (primary != null && primary.isAvailable()) {
            log.warn("Using primary source for unsupported market: feature={}, market={}",
                    feature, market);
            return primary;
        }

        // 回退到第一个可用的适配器
        return selectFirstAvailable();
    }

    private MarketDataAdapter selectFirstAvailable() {
        return adapters.stream()
                .filter(MarketDataAdapter::isAvailable)
                .findFirst()
                .orElseThrow(() -> new NoDataSourceException("No available data source"));
    }

    private MarketDataAdapter findAdapter(String name) {
        return adapters.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private boolean isMarketSupported(String source, String market) {
        if (market == null) {
            return true;
        }
        MarketDataProperties.DataSourceConfig config = properties.getSourceConfig(source);
        if (config == null || config.getMarkets() == null) {
            return true;
        }
        return config.getMarkets().contains(market);
    }

    /**
     * 无可用数据源异常。
     */
    public static class NoDataSourceException extends RuntimeException {
        public NoDataSourceException(String message) {
            super(message);
        }
    }
}
