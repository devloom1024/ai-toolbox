package com.devloom.ai.toolbox.investment.infrastructure.adapter;

import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 市场数据适配器注册表
 * <p>
 * 管理所有已注册的适配器,提供查询和选择功能
 */
@Slf4j
@Component
public class MarketDataAdapterRegistry {

    /**
     * 适配器注册表 (按名称索引)
     */
    private final Map<String, MarketDataAdapter> adaptersByName = new ConcurrentHashMap<>();

    /**
     * 适配器注册表 (按市场类型索引)
     */
    private final Map<MarketType, List<MarketDataAdapter>> adaptersByMarket = new ConcurrentHashMap<>();

    /**
     * 构造函数 - 自动注册所有适配器
     */
    @Autowired(required = false)
    public MarketDataAdapterRegistry(List<MarketDataAdapter> adapters) {
        if (adapters != null && !adapters.isEmpty()) {
            adapters.forEach(this::register);
            log.info("Registered {} market data adapters", adapters.size());
        } else {
            log.warn("No market data adapters found");
        }
    }

    /**
     * 注册适配器
     */
    public void register(MarketDataAdapter adapter) {
        String name = adapter.getName();
        if (adaptersByName.containsKey(name)) {
            log.warn("Adapter {} already registered, will be replaced", name);
        }

        adaptersByName.put(name, adapter);

        // 按市场类型索引
        for (MarketType market : adapter.getSupportedMarkets()) {
            adaptersByMarket.computeIfAbsent(market, k -> new java.util.ArrayList<>())
                .add(adapter);
        }

        log.info("Registered adapter: {}, supported markets: {}, priority: {}",
            name, adapter.getSupportedMarkets(), adapter.getPriority());
    }

    /**
     * 根据名称获取适配器
     */
    public MarketDataAdapter getAdapter(String name) {
        MarketDataAdapter adapter = adaptersByName.get(name);
        if (adapter == null) {
            throw new IllegalArgumentException("Adapter not found: " + name);
        }
        return adapter;
    }

    /**
     * 获取支持指定市场的所有适配器 (按优先级排序)
     */
    public List<MarketDataAdapter> getAdaptersForMarket(MarketType market) {
        List<MarketDataAdapter> adapters = adaptersByMarket.get(market);
        if (adapters == null || adapters.isEmpty()) {
            return List.of();
        }

        // 按优先级排序 (数字越小优先级越高)
        return adapters.stream()
            .sorted(Comparator.comparingInt(MarketDataAdapter::getPriority))
            .collect(Collectors.toList());
    }

    /**
     * 获取支持指定市场的健康适配器 (按优先级排序)
     */
    public List<MarketDataAdapter> getHealthyAdaptersForMarket(MarketType market) {
        return getAdaptersForMarket(market).stream()
            .filter(MarketDataAdapter::isHealthy)
            .collect(Collectors.toList());
    }

    /**
     * 获取所有适配器
     */
    public List<MarketDataAdapter> getAllAdapters() {
        return List.copyOf(adaptersByName.values());
    }

    /**
     * 检查是否有适配器支持指定市场
     */
    public boolean hasAdapterForMarket(MarketType market) {
        List<MarketDataAdapter> adapters = adaptersByMarket.get(market);
        return adapters != null && !adapters.isEmpty();
    }

    /**
     * 获取适配器统计信息
     */
    public AdapterStatistics getStatistics() {
        int totalAdapters = adaptersByName.size();
        int healthyAdapters = (int) adaptersByName.values().stream()
            .filter(MarketDataAdapter::isHealthy)
            .count();

        Map<MarketType, Integer> adapterCountByMarket = adaptersByMarket.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().size()
            ));

        return new AdapterStatistics(totalAdapters, healthyAdapters, adapterCountByMarket);
    }

    /**
     * 适配器统计信息
     */
    public record AdapterStatistics(
        int totalAdapters,
        int healthyAdapters,
        Map<MarketType, Integer> adapterCountByMarket
    ) {
    }
}
