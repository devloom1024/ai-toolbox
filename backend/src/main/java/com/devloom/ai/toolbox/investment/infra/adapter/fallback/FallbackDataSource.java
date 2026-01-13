package com.devloom.ai.toolbox.investment.infra.adapter.fallback;

import com.devloom.ai.toolbox.investment.domain.enums.DataSourceType;
import com.devloom.ai.toolbox.investment.domain.enums.KLinePeriod;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.*;
import com.devloom.ai.toolbox.investment.infra.adapter.AbstractMarketDataAdapter;
import com.devloom.ai.toolbox.investment.infra.cache.CacheKeyGenerator;
import com.devloom.ai.toolbox.investment.infra.quality.DataQualityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * 降级数据源适配器
 * <p>
 * 当主数据源不可用时,从Redis缓存获取历史数据作为降级方案
 */
@Slf4j
@Component
public class FallbackDataSource extends AbstractMarketDataAdapter {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheKeyGenerator keyGenerator;
    private final DataQualityValidator qualityValidator;

    @Autowired
    public FallbackDataSource(
        RedisTemplate<String, Object> redisTemplate,
        CacheKeyGenerator keyGenerator,
        DataQualityValidator qualityValidator
    ) {
        this.redisTemplate = redisTemplate;
        this.keyGenerator = keyGenerator;
        this.qualityValidator = qualityValidator;
        this.priority = 999; // 最低优先级
    }

    @Override
    public String getName() {
        return DataSourceType.FALLBACK.getName();
    }

    @Override
    public List<MarketType> getSupportedMarkets() {
        // 支持所有市场类型
        return List.of(MarketType.values());
    }

    @Override
    public QuoteData getQuote(String symbol, MarketType market) {
        String key = keyGenerator.generateQuoteKey(symbol, market);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof QuoteData) {
                QuoteData quote = (QuoteData) cached;
                quote.setSource(DataSourceType.FALLBACK);
                log.debug("Got fallback quote from cache: {}", symbol);
                return quote;
            }
            log.warn("No cached quote found for fallback: {}", key);
            return null;
        } catch (Exception e) {
            log.error("Failed to get fallback quote for: {}", symbol, e);
            return null;
        }
    }

    @Override
    public List<QuoteData> getBatchQuotes(List<String> symbols, MarketType market) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        return symbols.stream()
            .map(symbol -> getQuote(symbol, market))
            .filter(quote -> quote != null)
            .toList();
    }

    @Override
    public List<KLineData> getKLine(String symbol, MarketType market, KLinePeriod period, int limit) {
        String key = keyGenerator.generateKLineKey(symbol, market, period, limit);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                @SuppressWarnings("unchecked")
                List<KLineData> klines = (List<KLineData>) cached;
                log.debug("Got fallback KLine from cache: {}, count: {}", symbol, klines.size());
                return klines;
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to get fallback KLine for: {}", symbol, e);
            return List.of();
        }
    }

    @Override
    public FundamentalData getFundamental(String symbol, MarketType market) {
        String key = keyGenerator.generateFundamentalKey(symbol, market);

        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof FundamentalData) {
                FundamentalData data = (FundamentalData) cached;
                log.debug("Got fallback fundamental from cache: {}", symbol);
                return data;
            }
            return null;
        } catch (Exception e) {
            log.error("Failed to get fallback fundamental for: {}", symbol, e);
            return null;
        }
    }

    @Override
    public CapitalFlowData getCapitalFlow(String symbol, MarketType market) {
        // 资金流向数据不提供降级
        return null;
    }

    @Override
    protected boolean performHealthCheck() {
        // 降级数据源总是健康的 (只要Redis可用)
        try {
            redisTemplate.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            log.warn("Fallback health check failed: Redis not available", e);
            return false;
        }
    }

    /**
     * 存储到缓存 (供主数据源调用)
     */
    public void cacheQuote(String symbol, MarketType market, QuoteData data) {
        if (data == null) {
            return;
        }

        String key = keyGenerator.generateQuoteKey(symbol, market);
        try {
            // 缓存5分钟
            redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.error("Failed to cache quote for fallback: {}", key, e);
        }
    }

    /**
     * 存储K线到缓存
     */
    public void cacheKLine(String symbol, MarketType market, KLinePeriod period, int limit, List<KLineData> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        String key = keyGenerator.generateKLineKey(symbol, market, period, limit);
        try {
            redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("Failed to cache KLine for fallback: {}", key, e);
        }
    }
}
