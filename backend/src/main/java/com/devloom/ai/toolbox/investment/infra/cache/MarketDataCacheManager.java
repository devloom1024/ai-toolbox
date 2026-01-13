package com.devloom.ai.toolbox.investment.infra.cache;

import com.devloom.ai.toolbox.investment.config.CacheConfig;
import com.devloom.ai.toolbox.investment.config.MarketDataProperties;
import com.devloom.ai.toolbox.investment.domain.enums.KLinePeriod;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.*;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 多级缓存管理器
 * <p>
 * L1: 本地缓存 (Caffeine) - 低延迟
 * L2: 分布式缓存 (Redis) - 跨实例共享
 * L3: 数据源 - 原始数据
 */
@Slf4j
@Component
public class MarketDataCacheManager {

    private final MarketDataProperties properties;
    private final CacheKeyGenerator keyGenerator;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * L1本地缓存 - 行情数据
     */
    private final Cache<String, QuoteData> quoteL1Cache;

    /**
     * L1本地缓存 - K线数据
     */
    private final Cache<String, List<KLineData>> klineL1Cache;

    /**
     * L1本地缓存 - 基本面数据
     */
    private final Cache<String, FundamentalData> fundamentalL1Cache;

    /**
     * L1本地缓存 - 资金流向数据
     */
    private final Cache<String, CapitalFlowData> capitalFlowL1Cache;

    public MarketDataCacheManager(
        MarketDataProperties properties,
        CacheKeyGenerator keyGenerator,
        RedisTemplate<String, Object> redisTemplate
    ) {
        this.properties = properties;
        this.keyGenerator = keyGenerator;
        this.redisTemplate = redisTemplate;

        // 初始化L1缓存
        var l1Config = properties.getCache().getL1();
        this.quoteL1Cache = buildCaffeineCache(l1Config);
        this.klineL1Cache = buildCaffeineCache(l1Config);
        this.fundamentalL1Cache = buildCaffeineCache(l1Config);
        this.capitalFlowL1Cache = buildCaffeineCache(l1Config);

        log.info("MarketDataCacheManager initialized with L1 cache size: {}, expireAfterWrite: {}s",
            l1Config.getMaxSize(), l1Config.getExpireAfterWriteSeconds());
    }

    /**
     * 构建Caffeine缓存
     */
    private <T> Cache<String, T> buildCaffeineCache(CacheConfig config) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
            .maximumSize(config.getMaxSize())
            .expireAfterWrite(config.getExpireAfterWriteSeconds(), TimeUnit.SECONDS)
            .initialCapacity(config.getInitialCapacity());

        if (config.getRecordStats()) {
            builder.recordStats();
        }

        return builder.build();
    }

    /**
     * 获取行情数据 (多级缓存)
     */
    public QuoteData getQuote(String symbol, MarketType market, Supplier<QuoteData> dataLoader) {
        String key = keyGenerator.generateQuoteKey(symbol, market);

        // 1. 尝试从L1缓存获取
        QuoteData data = quoteL1Cache.getIfPresent(key);
        if (data != null) {
            log.debug("Hit L1 cache: {}", key);
            return data;
        }

        // 2. 尝试从L2 Redis缓存获取
        if (properties.getCache().getL2().getEnabled()) {
            try {
                data = (QuoteData) redisTemplate.opsForValue().get(key);
                if (data != null) {
                    log.debug("Hit L2 cache: {}", key);
                    // 回填L1缓存
                    quoteL1Cache.put(key, data);
                    return data;
                }
            } catch (Exception e) {
                log.warn("Failed to get from L2 cache: {}", key, e);
            }
        }

        // 3. 从数据源加载
        log.debug("Cache miss, loading from data source: {}", key);
        data = dataLoader.get();

        if (data != null && data.isValid()) {
            // 写入多级缓存
            putQuote(symbol, market, data);
        }

        return data;
    }

    /**
     * 存储行情数据到多级缓存
     */
    public void putQuote(String symbol, MarketType market, QuoteData data) {
        if (data == null) {
            return;
        }

        String key = keyGenerator.generateQuoteKey(symbol, market);

        // 写入L1缓存
        quoteL1Cache.put(key, data);

        // 写入L2缓存
        if (properties.getCache().getL2().getEnabled()) {
            try {
                long ttl = properties.getCache().getL2().getExpireAfterWriteSeconds();
                redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to put to L2 cache: {}", key, e);
            }
        }
    }

    /**
     * 获取K线数据 (多级缓存)
     */
    public List<KLineData> getKLine(
        String symbol,
        MarketType market,
        KLinePeriod period,
        int limit,
        Supplier<List<KLineData>> dataLoader
    ) {
        String key = keyGenerator.generateKLineKey(symbol, market, period, limit);

        // 1. L1缓存
        List<KLineData> data = klineL1Cache.getIfPresent(key);
        if (data != null) {
            log.debug("Hit L1 cache: {}", key);
            return data;
        }

        // 2. L2缓存
        if (properties.getCache().getL2().getEnabled()) {
            try {
                @SuppressWarnings("unchecked")
                List<KLineData> cached = (List<KLineData>) redisTemplate.opsForValue().get(key);
                if (cached != null) {
                    log.debug("Hit L2 cache: {}", key);
                    klineL1Cache.put(key, cached);
                    return cached;
                }
            } catch (Exception e) {
                log.warn("Failed to get from L2 cache: {}", key, e);
            }
        }

        // 3. 数据源加载
        log.debug("Cache miss, loading from data source: {}", key);
        data = dataLoader.get();

        if (data != null && !data.isEmpty()) {
            putKLine(symbol, market, period, limit, data);
        }

        return data;
    }

    /**
     * 存储K线数据到多级缓存
     */
    public void putKLine(
        String symbol,
        MarketType market,
        KLinePeriod period,
        int limit,
        List<KLineData> data
    ) {
        if (data == null || data.isEmpty()) {
            return;
        }

        String key = keyGenerator.generateKLineKey(symbol, market, period, limit);

        // L1缓存
        klineL1Cache.put(key, data);

        // L2缓存
        if (properties.getCache().getL2().getEnabled()) {
            try {
                long ttl = properties.getCache().getL2().getExpireAfterWriteSeconds();
                redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to put to L2 cache: {}", key, e);
            }
        }
    }

    /**
     * 获取基本面数据 (多级缓存)
     */
    public FundamentalData getFundamental(
        String symbol,
        MarketType market,
        Supplier<FundamentalData> dataLoader
    ) {
        String key = keyGenerator.generateFundamentalKey(symbol, market);

        // L1缓存
        FundamentalData data = fundamentalL1Cache.getIfPresent(key);
        if (data != null) {
            log.debug("Hit L1 cache: {}", key);
            return data;
        }

        // L2缓存
        if (properties.getCache().getL2().getEnabled()) {
            try {
                data = (FundamentalData) redisTemplate.opsForValue().get(key);
                if (data != null) {
                    log.debug("Hit L2 cache: {}", key);
                    fundamentalL1Cache.put(key, data);
                    return data;
                }
            } catch (Exception e) {
                log.warn("Failed to get from L2 cache: {}", key, e);
            }
        }

        // 数据源加载
        log.debug("Cache miss, loading from data source: {}", key);
        data = dataLoader.get();

        if (data != null && data.isValid()) {
            putFundamental(symbol, market, data);
        }

        return data;
    }

    /**
     * 存储基本面数据到多级缓存
     */
    public void putFundamental(String symbol, MarketType market, FundamentalData data) {
        if (data == null) {
            return;
        }

        String key = keyGenerator.generateFundamentalKey(symbol, market);

        // L1缓存
        fundamentalL1Cache.put(key, data);

        // L2缓存 (基本面数据可以缓存更久)
        if (properties.getCache().getL2().getEnabled()) {
            try {
                long ttl = properties.getCache().getL2().getExpireAfterWriteSeconds() * 6; // 30分钟
                redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failed to put to L2 cache: {}", key, e);
            }
        }
    }

    /**
     * 清除指定标的的所有缓存
     */
    public void evictSymbol(String symbol, MarketType market) {
        String quoteKey = keyGenerator.generateQuoteKey(symbol, market);
        quoteL1Cache.invalidate(quoteKey);

        if (properties.getCache().getL2().getEnabled()) {
            try {
                redisTemplate.delete(quoteKey);
            } catch (Exception e) {
                log.error("Failed to evict from L2 cache: {}", quoteKey, e);
            }
        }

        log.info("Evicted cache for symbol: {} in market: {}", symbol, market);
    }

    /**
     * 清除所有L1缓存
     */
    public void evictAllL1() {
        quoteL1Cache.invalidateAll();
        klineL1Cache.invalidateAll();
        fundamentalL1Cache.invalidateAll();
        capitalFlowL1Cache.invalidateAll();
        log.info("Evicted all L1 caches");
    }

    /**
     * 获取L1缓存统计信息
     */
    public CacheStatistics getL1Statistics() {
        CacheStats quoteStats = quoteL1Cache.stats();
        CacheStats klineStats = klineL1Cache.stats();

        return new CacheStatistics(
            quoteL1Cache.estimatedSize(),
            klineL1Cache.estimatedSize(),
            quoteStats.hitRate(),
            quoteStats.missRate(),
            quoteStats.evictionCount()
        );
    }

    /**
     * 缓存统计信息
     */
    public record CacheStatistics(
        long quoteSize,
        long klineSize,
        double hitRate,
        double missRate,
        long evictionCount
    ) {
    }
}
