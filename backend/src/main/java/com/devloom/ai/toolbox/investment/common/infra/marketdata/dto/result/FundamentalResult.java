package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 基本面结果。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class FundamentalResult {

    /** 标的代码。 */
    private String symbol;

    /** 总市值（元）。 */
    private BigDecimal marketCap;

    /** 流通市值（元）。 */
    private BigDecimal circulatingMarketCap;

    /** 总股本（股）。 */
    private BigDecimal totalShares;

    /** 流通股本（股）。 */
    private BigDecimal circulatingShares;

    /** 市盈率。 */
    private BigDecimal peRatio;

    /** 市盈率 TTM。 */
    private BigDecimal peRatioTtm;

    /** 市净率。 */
    private BigDecimal pbRatio;

    /** 市销率。 */
    private BigDecimal psRatio;

    /** 市现率。 */
    private BigDecimal pcRatio;

    /** 股息率（%）。 */
    private BigDecimal dividendYield;

    /** 每股净资产。 */
    private BigDecimal bvps;

    /** 每股收益。 */
    private BigDecimal eps;

    /** 每股收益同比增长率（%）。 */
    private BigDecimal epsYoy;

    /** 净资产收益率（%）。 */
    private BigDecimal roe;

    /** 净资产收益率-摊薄（%）。 */
    private BigDecimal roeDiluted;

    /** 毛利率（%）。 */
    private BigDecimal grossMargin;

    /** 净利率（%）。 */
    private BigDecimal netMargin;

    /** 最后更新时间。 */
    private Instant lastUpdated;
}
