package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 资金流向结果。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class CapitalFlowResult {

    /** 标的代码。 */
    private String symbol;

    /** 主力净流入（元）。 */
    private BigDecimal mainNetInflow;

    /** 主力净流入占比（%）。 */
    private BigDecimal mainNetInflowRate;

    /** 小单净流入（元）。 */
    private BigDecimal smallNetInflow;

    /** 中单净流入（元）。 */
    private BigDecimal mediumNetInflow;

    /** 大单净流入（元）。 */
    private BigDecimal largeNetInflow;

    /** 超大单净流入（元）。 */
    private BigDecimal superNetInflow;

    /** 最后更新时间。 */
    private Instant lastUpdated;
}
