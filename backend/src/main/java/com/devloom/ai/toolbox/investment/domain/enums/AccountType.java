package com.devloom.ai.toolbox.investment.domain.enums;

/**
 * 投资账号类型枚举
 *
 * @author huangkl
 */
public enum AccountType {
    /**
     * 券商账号（如东方财富、同花顺等）
     */
    BROKER,

    /**
     * 基金平台（如天天基金、蛋卷基金等）
     */
    FUND_PLATFORM,

    /**
     * 银行账号
     */
    BANK,

    /**
     * 支付宝
     */
    ALIPAY,

    /**
     * 其他（如雪球、东方财富自选股等）
     */
    OTHER
}
