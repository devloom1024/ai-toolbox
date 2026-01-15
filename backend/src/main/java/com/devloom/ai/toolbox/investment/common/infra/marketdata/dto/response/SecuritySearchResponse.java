package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 搜索结果。
 *
 * @author devloom
 */
@Getter
@Builder
public class SecuritySearchResponse {

    /** 标的代码（纯数字或字母）。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 市场类型：A_SHARE/A_SHARE/HK/US/ETF/FUND。 */
    private String market;

    /** 标的类型：STOCK/ETF/FUND。 */
    private String type;
}
