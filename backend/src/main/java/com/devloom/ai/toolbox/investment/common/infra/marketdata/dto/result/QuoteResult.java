package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 行情结果。
 *
 * @author devloom
 */
@Data
public class QuoteResult {

    /** 标的代码。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 当前价。 */
    private BigDecimal currentPrice;

    /** 开盘价。 */
    private BigDecimal openPrice;

    /** 最高价。 */
    private BigDecimal highPrice;

    /** 最低价。 */
    private BigDecimal lowPrice;

    /** 昨收价。 */
    private BigDecimal preClosePrice;

    /** 涨跌额。 */
    private BigDecimal change;

    /** 涨跌幅（%）。 */
    private BigDecimal changePercent;

    /** 成交量（股）。 */
    private Long volume;

    /** 成交额（元）。 */
    private BigDecimal turnover;

    /** 换手率（%）。 */
    private BigDecimal turnoverRate;

    /** 振幅（%）。 */
    private BigDecimal amplitude;

    /** 总市值（元）。 */
    private BigDecimal marketCap;

    /** 流通市值（元）。 */
    private BigDecimal circulatingMarketCap;

    /** 市盈率。 */
    private BigDecimal peRatio;

    /** 市净率。 */
    private BigDecimal pbRatio;

    /** 股息率（%）。 */
    private BigDecimal dividendYield;

    /** 最后更新时间。 */
    private Instant lastUpdated;
}
