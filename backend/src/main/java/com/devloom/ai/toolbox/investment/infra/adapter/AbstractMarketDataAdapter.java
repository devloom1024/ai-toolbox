package com.devloom.ai.toolbox.investment.infra.adapter;

import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.CapitalFlowData;
import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import com.devloom.ai.toolbox.investment.domain.model.StockSearchData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 市场数据适配器抽象基类
 * <p>
 * 提供通用功能和默认实现
 */
@Slf4j
public abstract class AbstractMarketDataAdapter implements MarketDataAdapter {

    /**
     * 健康状态标记
     */
    protected final AtomicBoolean healthy = new AtomicBoolean(true);

    /**
     * 适配器优先级 (默认)
     */
    protected int priority = 99;

    @Override
    public boolean supportsMarket(MarketType market) {
        return getSupportedMarkets().contains(market);
    }

    @Override
    public List<QuoteData> getBatchQuotes(List<String> symbols, MarketType market) {
        // 默认实现: 循环调用单个查询
        List<QuoteData> results = new ArrayList<>();
        for (String symbol : symbols) {
            try {
                QuoteData quote = getQuote(symbol, market);
                if (quote != null) {
                    results.add(quote);
                }
            } catch (Exception e) {
                log.warn("Failed to get quote for symbol: {}, error: {}", symbol, e.getMessage());
            }
        }
        return results;
    }

    @Override
    public CapitalFlowData getCapitalFlow(String symbol, MarketType market) {
        // 默认实现: 不支持
        log.warn("Adapter {} does not support capital flow data", getName());
        return null;
    }

    @Override
    public boolean isHealthy() {
        return healthy.get();
    }

    @Override
    public boolean healthCheck() {
        try {
            // 默认实现: 调用健康检查接口
            boolean result = performHealthCheck();
            healthy.set(result);
            return result;
        } catch (Exception e) {
            log.error("Health check failed for adapter: {}", getName(), e);
            healthy.set(false);
            return false;
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    /**
     * 设置优先级
     *
     * @param priority 优先级
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * 执行健康检查 (子类实现)
     *
     * @return true-健康, false-不健康
     */
    protected abstract boolean performHealthCheck();

    /**
     * 标记适配器不健康
     */
    protected void markUnhealthy() {
        healthy.set(false);
        log.warn("Adapter {} marked as unhealthy", getName());
    }

    /**
     * 标记适配器健康
     */
    protected void markHealthy() {
        if (!healthy.get()) {
            healthy.set(true);
            log.info("Adapter {} recovered to healthy", getName());
        }
    }

    /**
     * 校验参数
     */
    protected void validateParameters(String symbol, MarketType market) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        if (market == null) {
            throw new IllegalArgumentException("Market type cannot be null");
        }
        if (!supportsMarket(market)) {
            throw new UnsupportedOperationException(
                String.format("Adapter %s does not support market: %s", getName(), market)
            );
        }
    }

    /**
     * 校验搜索参数
     */
    protected void validateSearchParameters(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("Keyword cannot be null or empty");
        }
        if (keyword.length() < 1) {
            throw new IllegalArgumentException("Keyword must be at least 1 character");
        }
    }

    @Override
    public List<StockSearchData> search(String keyword, MarketType market, int limit) {
        // 默认实现: 不支持搜索功能
        log.warn("Adapter {} does not support search functionality", getName());
        return Collections.emptyList();
    }
}
