package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.domain.enums.SecurityType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 搜索结果。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class SecuritySearchResult {

    /** 标的代码（纯数字或字母）。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 市场类型。 */
    private MarketType market;

    /** 标的类型。 */
    private SecurityType type;
}
