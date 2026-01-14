package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 基本面请求。
 *
 * @author claude
 */
@Getter
@Builder
public class FundamentalRequest {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private String market;
}
