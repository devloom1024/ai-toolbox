package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 资金流向请求。
 *
 * @author devloom
 */
@Getter
@Builder
public class CapitalFlowRequest {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private String market;
}
