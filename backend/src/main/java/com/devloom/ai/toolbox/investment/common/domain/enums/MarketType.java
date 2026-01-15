package com.devloom.ai.toolbox.investment.watchlist.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 市场类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum MarketType {

    /** A股。 */
    A_SHARE("A_SHARE", "A股"),

    /** 港股。 */
    HK("HK", "港股"),

    /** 美股。 */
    US("US", "美股"),

    /** ETF。 */
    ETF("ETF", "ETF"),

    /** 基金。 */
    FUND("FUND", "基金"),

    /** 全部市场。 */
    ALL("ALL", "全部");

    private final String code;
    private final String description;
}
