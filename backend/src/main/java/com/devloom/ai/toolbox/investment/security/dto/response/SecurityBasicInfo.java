package com.devloom.ai.toolbox.investment.security.dto.response;

import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.SecurityType;
import lombok.Builder;
import lombok.Getter;

/**
 * 标的基本信息。
 *
 * @author claude
 */
@Getter
@Builder
public class SecurityBasicInfo {

    /** 标的代码。 */
    private String symbol;

    /** 标的名称。 */
    private String name;

    /** 市场类型。 */
    private MarketType market;

    /** 标的类型。 */
    private SecurityType type;

    /** 交易所。 */
    private String exchange;

    /** 上市日期。 */
    private java.time.LocalDate listDate;

    /** 退市日期。 */
    private java.time.LocalDate delistDate;

    /** 公司全称。 */
    private String fullName;

    /** 所属行业。 */
    private String industry;

    /** 所属地域。 */
    private String area;
}
