package com.devloom.ai.toolbox.investment.common.infra.marketdata.router;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.config.MarketDataProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据源路由器。
 *
 * <p>按功能类型选择数据源，支持配置化路由和故障转移。</p>
 *
 * @author devloom
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceRouter {
    private final MarketDataProperties properties;
    private final List<MarketDataAdapter> adapters;

    /**
     * 根据功能获取数据源适配器。
     *
     * @param feature 功能路由键枚举
     * @param market 市场类型
     * @return 数据源适配器
     */
    public MarketDataAdapter select(MarketDataFeature feature, MarketType market) {
        MarketDataProperties.FeatureRouting routing = properties.getRouting(feature);

        if (routing == null) {
            // 默认使用第一个可用的适配器
            return selectFirstAvailable();
        }

        String primarySource = routing.getPrimary();

        // 1. 检查主数据源是否支持该市场且已启用
        if (isMarketAndFeatureSupported(primarySource, market, feature)) {
            MarketDataAdapter adapter = findAdapter(primarySource);
            if (adapter != null && isEnabled(adapter) && adapter.isAvailable()) {
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
        return select(MarketDataFeature.SEARCH, null);
    }

    /**
     * 获取 K 线数据源适配器。
     */
    public MarketDataAdapter selectForKline(MarketType market) {
        return select(MarketDataFeature.KLINE, market);
    }

    /**
     * 获取基本面数据源适配器。
     */
    public MarketDataAdapter selectForFundamental(MarketType market) {
        return select(MarketDataFeature.FUNDAMENTAL, market);
    }

    /**
     * 获取财务指标数据源适配器。
     */
    public MarketDataAdapter selectForFinancial(MarketType market) {
        return select(MarketDataFeature.FINANCIAL, market);
    }

    /**
     * 获取资金流向数据源适配器。
     */
    public MarketDataAdapter selectForCapitalFlow(MarketType market) {
        return select(MarketDataFeature.CAPITAL_FLOW, market);
    }

    private MarketDataAdapter selectWithFallback(MarketDataProperties.FeatureRouting routing,
                                                 MarketDataFeature feature, MarketType market) {
        List<String> chain = routing.getFallback();

        if (chain == null || chain.isEmpty()) {
            // 无故障转移链，回退到主数据源
            return selectPrimaryOrFirst(routing.getPrimary(), feature, market);
        }

        for (String sourceName : chain) {
            if (sourceName.equals(routing.getPrimary())) {
                continue;
            }

            if (!isMarketAndFeatureSupported(sourceName, market, feature)) {
                continue;
            }

            MarketDataAdapter adapter = findAdapter(sourceName);
            if (adapter != null && isEnabled(adapter) && adapter.isAvailable()) {
                log.info("Selected fallback source: {} for feature: {}, market: {}",
                        sourceName, feature.getKey(), market);
                return adapter;
            }
        }

        // 回退到主数据源
        return selectPrimaryOrFirst(routing.getPrimary(), feature, market);
    }

    private MarketDataAdapter selectPrimaryOrFirst(String primarySource, MarketDataFeature feature, MarketType market) {
        MarketDataAdapter primary = findAdapter(primarySource);
        if (primary != null && isEnabled(primary) && primary.isAvailable()) {
            log.warn("Using primary source for unsupported market: feature={}, market={}",
                    feature, market);
            return primary;
        }

        // 回退到第一个可用的适配器
        return selectFirstAvailable();
    }

    private MarketDataAdapter selectFirstAvailable() {
        return adapters.stream()
                .filter(this::isEnabled)
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

    /**
     * 检查适配器是否在配置中启用。
     */
    private boolean isEnabled(MarketDataAdapter adapter) {
        MarketDataProperties.DataSourceConfig config = properties.getSourceConfig(adapter.getName());
        if (config == null) {
            // 默认启用
            return true;
        }
        return config.isEnabled();
    }

    /**
     * 检查数据源是否支持指定市场和功能。
     */
    private boolean isMarketAndFeatureSupported(String source, MarketType market, MarketDataFeature feature) {
        MarketDataAdapter adapter = findAdapter(source);
        if (adapter == null) {
            return false;
        }

        // 检查功能支持
        if (!adapter.getSupportedFeatures().contains(feature)) {
            return false;
        }

        // 检查市场支持
        if (market != null && !adapter.getSupportedMarkets().contains(market)) {
            return false;
        }

        return true;
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
