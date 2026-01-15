package com.devloom.ai.toolbox.investment.common.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 标的类型枚举。
 */
@Getter
@RequiredArgsConstructor
public enum SecurityType {

    /** 股票。 */
    STOCK("STOCK", "股票"),

    /** ETF。 */
    ETF("ETF", "ETF"),

    /** 基金。 */
    FUND("FUND", "基金");

    private final String code;
    private final String description;
}
