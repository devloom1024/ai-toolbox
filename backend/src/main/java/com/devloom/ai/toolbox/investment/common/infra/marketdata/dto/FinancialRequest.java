package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务指标请求。
 *
 * @author claude
 */
@Getter
@Builder
public class FinancialRequest {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private String market;
}
