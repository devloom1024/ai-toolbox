package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 行情请求。
 *
 * @author devloom
 */
@Getter
@Builder
public class QuoteRequest {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private String market;
}
