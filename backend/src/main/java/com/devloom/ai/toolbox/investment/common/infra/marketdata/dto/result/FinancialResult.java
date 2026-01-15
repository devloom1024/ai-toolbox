package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务指标结果。
 *
 * @author devloom
 */
@Data
public class FinancialResult {

    /** 标的代码。 */
    private String symbol;

    /** 报告期列表。 */
    private List<String> reportDates;

    /** 财务指标列表。 */
    private List<FinancialIndicator> indicators;

    @Data
    public static class FinancialIndicator {

        /** 报告期。 */
        private String reportDate;

        /** 营业收入（元）。 */
        private BigDecimal revenue;

        /** 净利润（元）。 */
        private BigDecimal netProfit;

        /** 营业利润（元）。 */
        private BigDecimal operatingProfit;

        /** 总资产（元）。 */
        private BigDecimal totalAssets;

        /** 总负债（元）。 */
        private BigDecimal totalLiabilities;

        /** 股东权益（元）。 */
        private BigDecimal shareholdersEquity;

        /** 净资产收益率（%）。 */
        private BigDecimal roe;

        /** 毛利率（%）。 */
        private BigDecimal grossMargin;

        /** 净利率（%）。 */
        private BigDecimal netMargin;

        /** 每股收益-基本。 */
        private BigDecimal basicEps;

        /** 每股收益-摊薄。 */
        private BigDecimal dilutedEps;
    }
}
