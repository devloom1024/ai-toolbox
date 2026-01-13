package com.devloom.ai.toolbox.investment.domain.enums;

/**
 * 数据源类型
 */
public enum DataSourceType {
    /**
     * Akshare数据源 (A股主力)
     */
    AKSHARE("akshare", "Akshare", 1),

    /**
     * Yfinance数据源 (美股主力)
     */
    YFINANCE("yfinance", "Yfinance", 1),

    /**
     * Tushare数据源 (备用)
     */
    TUSHARE("tushare", "Tushare", 2),

    /**
     * Web API数据源 (备用)
     */
    WEB_API("webapi", "WebAPI", 3),

    /**
     * 降级数据源 (缓存)
     */
    FALLBACK("fallback", "Fallback", 999);

    private final String name;
    private final String displayName;
    private final int defaultPriority;

    DataSourceType(String name, String displayName, int defaultPriority) {
        this.name = name;
        this.displayName = displayName;
        this.defaultPriority = defaultPriority;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    /**
     * 根据名称获取数据源类型
     */
    public static DataSourceType fromName(String name) {
        for (DataSourceType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data source name: " + name);
    }
}
