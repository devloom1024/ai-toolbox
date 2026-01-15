package com.devloom.ai.toolbox.investment.common.infra.marketdata.router;

import lombok.Getter;

/**
 * 市场数据功能路由键枚举。
 *
 * <p>定义了各类市场数据功能的路由键，用于数据源路由选择。</p>
 *
 * @author devloom
 */
@Getter
public enum MarketDataFeature {

    /** 搜索功能路由键。 */
    SEARCH("search"),

    /** K 线功能路由键。 */
    KLINE("kline"),

    /** 基本面功能路由键。 */
    FUNDAMENTAL("fundamental"),

    /** 财务指标功能路由键。 */
    FINANCIAL("financial"),

    /** 资金流向功能路由键。 */
    CAPITAL_FLOW("capitalFlow");

    private final String key;

    MarketDataFeature(String key) {
        this.key = key;
    }

}
