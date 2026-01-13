package com.devloom.ai.toolbox.investment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 资金流向数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapitalFlowData {
    /**
     * 标的代码
     */
    private String symbol;

    /**
     * 标的名称
     */
    private String name;

    /**
     * 数据日期
     */
    private LocalDate date;

    /**
     * 主力净流入 (万元)
     */
    private BigDecimal mainNetInflow;

    /**
     * 主力净流入占比 (%)
     */
    private BigDecimal mainNetInflowRate;

    /**
     * 超大单净流入 (万元)
     */
    private BigDecimal superLargeNetInflow;

    /**
     * 超大单净流入占比 (%)
     */
    private BigDecimal superLargeNetInflowRate;

    /**
     * 大单净流入 (万元)
     */
    private BigDecimal largeNetInflow;

    /**
     * 大单净流入占比 (%)
     */
    private BigDecimal largeNetInflowRate;

    /**
     * 中单净流入 (万元)
     */
    private BigDecimal mediumNetInflow;

    /**
     * 中单净流入占比 (%)
     */
    private BigDecimal mediumNetInflowRate;

    /**
     * 小单净流入 (万元)
     */
    private BigDecimal smallNetInflow;

    /**
     * 小单净流入占比 (%)
     */
    private BigDecimal smallNetInflowRate;

    /**
     * 北向资金净流入 (万元) - 仅A股
     */
    private BigDecimal northboundNetInflow;

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return symbol != null
            && date != null
            && mainNetInflow != null;
    }

    /**
     * 判断是否为资金流入
     */
    public boolean isNetInflow() {
        return mainNetInflow != null && mainNetInflow.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 判断是否为资金流出
     */
    public boolean isNetOutflow() {
        return mainNetInflow != null && mainNetInflow.compareTo(BigDecimal.ZERO) < 0;
    }
}
