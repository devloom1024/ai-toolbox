package com.devloom.ai.toolbox.investment.infra.router;

import com.devloom.ai.toolbox.investment.config.MarketDataProperties;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.infra.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.infra.adapter.MarketDataAdapterRegistry;
import com.devloom.ai.toolbox.investment.infra.health.DataSourceHealthChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源路由器
 * <p>
 * 根据市场类型和健康状态选择最优数据源
 */
@Slf4j
@Component
public class DataSourceRouter {

    private final MarketDataAdapterRegistry adapterRegistry;
    private final DataSourceHealthChecker healthChecker;
    private final MarketDataProperties properties;

    public DataSourceRouter(
        MarketDataAdapterRegistry adapterRegistry,
        DataSourceHealthChecker healthChecker,
        MarketDataProperties properties
    ) {
        this.adapterRegistry = adapterRegistry;
        this.healthChecker = healthChecker;
        this.properties = properties;
    }

    /**
     * 选择最优数据源 (支持指定市场)
     *
     * @param market 市场类型
     * @return 最优数据源适配器
     */
    public MarketDataAdapter selectAdapter(MarketType market) {
        List<MarketDataAdapter> candidates = getCandidateAdapters(market);

        if (candidates.isEmpty()) {
            log.warn("No available adapter for market: {}, will use fallback", market);
            return getFallbackAdapter();
        }

        // 按优先级和健康度选择
        return candidates.stream()
            .min(Comparator.comparingInt(MarketDataAdapter::getPriority))
            .orElseGet(this::getFallbackAdapter);
    }

    /**
     * 获取候选数据源列表 (已过滤不健康的)
     */
    private List<MarketDataAdapter> getCandidateAdapters(MarketType market) {
        // 1. 获取支持该市场的所有适配器
        List<MarketDataAdapter> adapters = adapterRegistry.getAdaptersForMarket(market);

        if (adapters.isEmpty()) {
            log.warn("No adapter registered for market: {}", market);
            return List.of();
        }

        // 2. 过滤健康的适配器
        List<MarketDataAdapter> healthyAdapters = adapters.stream()
            .filter(adapter -> healthChecker.isHealthy(adapter.getName()))
            .collect(Collectors.toList());

        if (!healthyAdapters.isEmpty()) {
            return healthyAdapters;
        }

        // 3. 如果没有完全健康的,尝试使用最近成功的适配器
        List<MarketDataAdapter> recentlySuccessful = adapters.stream()
            .filter(adapter -> healthChecker.isRecentlySuccessful(adapter.getName()))
            .collect(Collectors.toList());

        if (!recentlySuccessful.isEmpty()) {
            log.warn("No healthy adapter for market: {}, using recently successful one", market);
            return recentlySuccessful;
        }

        // 4. 返回所有适配器(降级方案)
        log.warn("No healthy or recently successful adapter for market: {}, using first available", market);
        return adapters.subList(0, 1);
    }

    /**
     * 获取降级适配器 (从缓存获取)
     */
    private MarketDataAdapter getFallbackAdapter() {
        // 降级适配器由Spring容器提供
        try {
            return adapterRegistry.getAdapter("fallback");
        } catch (IllegalArgumentException e) {
            log.error("No fallback adapter available");
            throw new RuntimeException("No available data source for market data", e);
        }
    }

    /**
     * 批量选择数据源
     *
     * @param market 市场类型
     * @return 最优数据源列表 (按优先级排序)
     */
    public List<MarketDataAdapter> selectAdapters(MarketType market, int limit) {
        return getCandidateAdapters(market).stream()
            .limit(limit)
            .collect(Collectors.toList());
    }

    /**
     * 检查指定市场是否有可用的数据源
     */
    public boolean hasAvailableAdapter(MarketType market) {
        List<MarketDataAdapter> candidates = getCandidateAdapters(market);
        return !candidates.isEmpty();
    }

    /**
     * 获取所有数据源的状态信息
     */
    public List<AdapterStatus> getAllAdapterStatus() {
        return adapterRegistry.getAllAdapters().stream()
            .map(adapter -> new AdapterStatus(
                adapter.getName(),
                adapter.getSupportedMarkets(),
                adapter.getPriority(),
                healthChecker.getHealthInfo(adapter.getName())
            ))
            .collect(Collectors.toList());
    }

    /**
     * 适配器状态信息
     */
    public record AdapterStatus(
        String name,
        List<MarketType> supportedMarkets,
        int priority,
        DataSourceHealthChecker.HealthInfo healthInfo
    ) {
    }
}
