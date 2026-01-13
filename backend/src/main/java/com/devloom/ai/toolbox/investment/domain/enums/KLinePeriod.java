package com.devloom.ai.toolbox.investment.domain.enums;

/**
 * K线周期
 */
public enum KLinePeriod {
    /**
     * 1分钟
     */
    MIN_1("1min", "1分钟", 1),

    /**
     * 5分钟
     */
    MIN_5("5min", "5分钟", 5),

    /**
     * 15分钟
     */
    MIN_15("15min", "15分钟", 15),

    /**
     * 30分钟
     */
    MIN_30("30min", "30分钟", 30),

    /**
     * 60分钟
     */
    MIN_60("60min", "60分钟", 60),

    /**
     * 日线
     */
    DAILY("daily", "日线", 1440),

    /**
     * 周线
     */
    WEEKLY("weekly", "周线", 10080),

    /**
     * 月线
     */
    MONTHLY("monthly", "月线", 43200);

    private final String code;
    private final String displayName;
    private final int minutes;

    KLinePeriod(String code, String displayName, int minutes) {
        this.code = code;
        this.displayName = displayName;
        this.minutes = minutes;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMinutes() {
        return minutes;
    }

    /**
     * 根据代码获取周期
     */
    public static KLinePeriod fromCode(String code) {
        for (KLinePeriod period : values()) {
            if (period.code.equalsIgnoreCase(code)) {
                return period;
            }
        }
        throw new IllegalArgumentException("Unknown period code: " + code);
    }
}
