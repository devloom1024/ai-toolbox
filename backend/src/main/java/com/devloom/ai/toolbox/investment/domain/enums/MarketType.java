package com.devloom.ai.toolbox.investment.domain.enums;

/**
 * 市场类型
 */
public enum MarketType {
    /**
     * A股市场
     */
    A_SHARE("A股", "CN"),

    /**
     * 港股市场
     */
    HK("港股", "HK"),

    /**
     * 美股市场
     */
    US("美股", "US"),

    /**
     * ETF基金
     */
    ETF("ETF", "ETF"),

    /**
     * 场外基金
     */
    FUND("场外基金", "FUND");

    private final String displayName;
    private final String code;

    MarketType(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCode() {
        return code;
    }

    /**
     * 根据代码获取市场类型
     */
    public static MarketType fromCode(String code) {
        for (MarketType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown market code: " + code);
    }
}
