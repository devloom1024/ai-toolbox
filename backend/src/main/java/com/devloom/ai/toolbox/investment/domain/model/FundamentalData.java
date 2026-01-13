package com.devloom.ai.toolbox.investment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 基本面数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundamentalData {
    /**
     * 标的代码
     */
    private String symbol;

    /**
     * 标的名称
     */
    private String name;

    /**
     * 市盈率(TTM)
     */
    private BigDecimal peTtm;

    /**
     * 市盈率(静态)
     */
    private BigDecimal peStatic;

    /**
     * 市净率
     */
    private BigDecimal pb;

    /**
     * 市销率
     */
    private BigDecimal ps;

    /**
     * 净资产收益率 ROE (%)
     */
    private BigDecimal roe;

    /**
     * 总资产收益率 ROA (%)
     */
    private BigDecimal roa;

    /**
     * 毛利率 (%)
     */
    private BigDecimal grossProfitMargin;

    /**
     * 净利率 (%)
     */
    private BigDecimal netProfitMargin;

    /**
     * 每股收益 EPS
     */
    private BigDecimal eps;

    /**
     * 每股净资产
     */
    private BigDecimal bps;

    /**
     * 总市值 (亿元)
     */
    private BigDecimal totalMarketCap;

    /**
     * 流通市值 (亿元)
     */
    private BigDecimal circulationMarketCap;

    /**
     * 总股本 (万股)
     */
    private BigDecimal totalShares;

    /**
     * 流通股本 (万股)
     */
    private BigDecimal circulationShares;

    /**
     * 营业收入 (亿元)
     */
    private BigDecimal revenue;

    /**
     * 净利润 (亿元)
     */
    private BigDecimal netProfit;

    /**
     * 营业收入同比增长率 (%)
     */
    private BigDecimal revenueGrowthRate;

    /**
     * 净利润同比增长率 (%)
     */
    private BigDecimal netProfitGrowthRate;

    /**
     * 资产负债率 (%)
     */
    private BigDecimal debtToAssetRatio;

    /**
     * 流动比率
     */
    private BigDecimal currentRatio;

    /**
     * 速动比率
     */
    private BigDecimal quickRatio;

    /**
     * 数据日期
     */
    private LocalDate dataDate;

    /**
     * 报告期
     */
    private String reportPeriod;

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return symbol != null
            && (peTtm != null || pb != null || eps != null);
    }
}
