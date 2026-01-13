package com.devloom.ai.toolbox.investment.infrastructure.cache;

import com.devloom.ai.toolbox.investment.domain.enums.KLinePeriod;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 缓存Key生成器
 * <p>
 * 统一管理所有缓存Key的命名规范
 */
@Slf4j
@Component
public class CacheKeyGenerator {

    /**
     * 缓存Key前缀
     */
    private static final String PREFIX = "investment:market-data";

    /**
     * 分隔符
     */
    private static final String SEPARATOR = ":";

    /**
     * 生成实时行情缓存Key
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 缓存Key
     */
    public String generateQuoteKey(String symbol, MarketType market) {
        return String.join(SEPARATOR,
            PREFIX,
            "quote",
            market.getCode().toLowerCase(),
            normalizeSymbol(symbol)
        );
    }

    /**
     * 生成批量行情缓存Key前缀
     *
     * @param market 市场类型
     * @return 缓存Key前缀
     */
    public String generateQuoteKeyPrefix(MarketType market) {
        return String.join(SEPARATOR,
            PREFIX,
            "quote",
            market.getCode().toLowerCase(),
            ""
        );
    }

    /**
     * 生成K线数据缓存Key
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @param period K线周期
     * @param limit  数据条数
     * @return 缓存Key
     */
    public String generateKLineKey(String symbol, MarketType market, KLinePeriod period, int limit) {
        return String.join(SEPARATOR,
            PREFIX,
            "kline",
            market.getCode().toLowerCase(),
            normalizeSymbol(symbol),
            period.getCode(),
            String.valueOf(limit)
        );
    }

    /**
     * 生成基本面数据缓存Key
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 缓存Key
     */
    public String generateFundamentalKey(String symbol, MarketType market) {
        return String.join(SEPARATOR,
            PREFIX,
            "fundamental",
            market.getCode().toLowerCase(),
            normalizeSymbol(symbol)
        );
    }

    /**
     * 生成资金流向缓存Key
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 缓存Key
     */
    public String generateCapitalFlowKey(String symbol, MarketType market) {
        return String.join(SEPARATOR,
            PREFIX,
            "capital-flow",
            market.getCode().toLowerCase(),
            normalizeSymbol(symbol)
        );
    }

    /**
     * 标准化标的代码 (统一大写、去空格)
     */
    private String normalizeSymbol(String symbol) {
        if (symbol == null) {
            return "";
        }
        return symbol.trim().toUpperCase();
    }

    /**
     * 解析缓存Key获取标的代码
     */
    public String parseSymbolFromKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        String[] parts = key.split(SEPARATOR);
        if (parts.length >= 4) {
            return parts[3]; // 标的代码在第4个位置
        }
        return null;
    }
}
