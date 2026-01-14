package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * K 线请求。
 *
 * @author claude
 */
@Getter
@Builder
public class KlineRequest {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private String market;

    /** K 线周期：D/W/M/5/15/30/60。 */
    private String period;

    /** 开始日期 (YYYYMMDD)。 */
    private String startDate;

    /** 结束日期 (YYYYMMDD)。 */
    private String endDate;

    /** 数据条数限制。 */
    private int limit = 500;
}
