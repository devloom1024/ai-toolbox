package com.devloom.ai.toolbox.investment.search.dto.response;

import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.SecurityType;
import lombok.Builder;
import lombok.Getter;

/**
 * 搜索结果项。
 *
 * @author claude
 */
@Getter
@Builder
public class SecuritySearchItem {

    /** 标的代码（不带前缀，纯数字或字母）。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 市场类型。 */
    private MarketType market;

    /** 标的类型。 */
    private SecurityType type;
}
