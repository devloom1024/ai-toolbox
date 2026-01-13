package com.devloom.ai.toolbox.investment.infra.adapter.akshare;

import com.devloom.ai.toolbox.investment.domain.enums.DataSourceType;
import com.devloom.ai.toolbox.investment.domain.enums.KLinePeriod;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.*;
import com.devloom.ai.toolbox.investment.infra.adapter.AbstractMarketDataAdapter;
import com.devloom.ai.toolbox.investment.infra.health.DataSourceHealthChecker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Akshare数据源适配器
 * <p>
 * 通过HTTP调用Python服务获取A股数据
 */
@Slf4j
@Component
public class AkshareDataSource extends AbstractMarketDataAdapter {

    private static final List<MarketType> SUPPORTED_MARKETS = Arrays.asList(
        MarketType.A_SHARE,
        MarketType.ETF,
        MarketType.FUND
    );

    private final AkshareClient client;
    private final AkshareResponseMapper mapper;
    private final DataSourceHealthChecker healthChecker;

    @Autowired
    public AkshareDataSource(
        AkshareClient client,
        AkshareResponseMapper mapper,
        DataSourceHealthChecker healthChecker
    ) {
        this.client = client;
        this.mapper = mapper;
        this.healthChecker = healthChecker;
        this.priority = 1; // A股主数据源
    }

    @Override
    public String getName() {
        return DataSourceType.AKSHARE.getName();
    }

    @Override
    public List<MarketType> getSupportedMarkets() {
        return SUPPORTED_MARKETS;
    }

    @Override
    public QuoteData getQuote(String symbol, MarketType market) {
        validateParameters(symbol, market);

        try {
            QuoteData quote = client.getQuote(symbol);
            if (quote != null) {
                quote.setMarket(market);
                quote.setSource(DataSourceType.AKSHARE);
                healthChecker.recordSuccess(getName());
                return quote;
            }
            healthChecker.recordFailure(getName());
            return null;
        } catch (Exception e) {
            log.error("Failed to get quote for symbol: {}", symbol, e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to get quote from Akshare", e);
        }
    }

    @Override
    public List<QuoteData> getBatchQuotes(List<String> symbols, MarketType market) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        try {
            List<QuoteData> quotes = client.getBatchQuotes(symbols);
            quotes.forEach(quote -> {
                quote.setMarket(market);
                quote.setSource(DataSourceType.AKSHARE);
            });
            healthChecker.recordSuccess(getName());
            return quotes;
        } catch (Exception e) {
            log.error("Failed to get batch quotes, count: {}", symbols.size(), e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to get batch quotes from Akshare", e);
        }
    }

    @Override
    public List<KLineData> getKLine(String symbol, MarketType market, KLinePeriod period, int limit) {
        validateParameters(symbol, market);

        try {
            String aksharePeriod = mapToAksharePeriod(period);
            List<KLineData> klines = client.getKLine(symbol, aksharePeriod, limit);
            healthChecker.recordSuccess(getName());
            return klines;
        } catch (Exception e) {
            log.error("Failed to get KLine for symbol: {}, period: {}", symbol, period, e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to get KLine from Akshare", e);
        }
    }

    @Override
    public FundamentalData getFundamental(String symbol, MarketType market) {
        validateParameters(symbol, market);

        try {
            FundamentalData data = client.getFundamental(symbol);
            if (data != null) {
                healthChecker.recordSuccess(getName());
                return data;
            }
            healthChecker.recordFailure(getName());
            return null;
        } catch (Exception e) {
            log.error("Failed to get fundamental for symbol: {}", symbol, e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to get fundamental from Akshare", e);
        }
    }

    @Override
    public CapitalFlowData getCapitalFlow(String symbol, MarketType market) {
        validateParameters(symbol, market);

        try {
            CapitalFlowData data = client.getCapitalFlow(symbol);
            healthChecker.recordSuccess(getName());
            return data;
        } catch (Exception e) {
            log.error("Failed to get capital flow for symbol: {}", symbol, e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to get capital flow from Akshare", e);
        }
    }

    @Override
    public List<StockSearchResult> search(String keyword, MarketType market, int limit) {
        validateSearchParameters(keyword);

        // 如果指定了市场但不支持,返回空
        if (market != null && !supportsMarket(market)) {
            log.debug("Market {} not supported by Akshare adapter", market);
            return List.of();
        }

        try {
            List<StockSearchResult> results = client.search(keyword, market, limit);
            healthChecker.recordSuccess(getName());
            return results;
        } catch (Exception e) {
            log.error("Failed to search for keyword: {}", keyword, e);
            healthChecker.recordFailure(getName());
            throw new RuntimeException("Failed to search from Akshare", e);
        }
    }

    @Override
    protected boolean performHealthCheck() {
        try {
            return client.healthCheck();
        } catch (Exception e) {
            log.error("Health check failed for Akshare", e);
            return false;
        }
    }

    /**
     * 映射K线周期到Akshare格式
     */
    private String mapToAksharePeriod(KLinePeriod period) {
        return switch (period) {
            case MIN_1 -> "1";
            case MIN_5 -> "5";
            case MIN_15 -> "15";
            case MIN_30 -> "30";
            case MIN_60 -> "60";
            case DAILY -> "daily";
            case WEEKLY -> "weekly";
            case MONTHLY -> "monthly";
        };
    }

    /**
     * 根据市场类型获取股票列表 (用于缓存预热)
     */
    public List<String> getPopularStocks(MarketType market) {
        try {
            return client.getPopularStocks(market);
        } catch (Exception e) {
            log.error("Failed to get popular stocks for market: {}", market, e);
            return List.of();
        }
    }
}
