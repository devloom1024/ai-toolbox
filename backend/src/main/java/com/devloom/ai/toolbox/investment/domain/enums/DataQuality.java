package com.devloom.ai.toolbox.investment.domain.enums;

/**
 * 数据质量等级
 */
public enum DataQuality {
    /**
     * 优秀 - 数据完整、准确、新鲜
     */
    EXCELLENT(5, "优秀"),

    /**
     * 良好 - 数据基本完整和准确
     */
    GOOD(4, "良好"),

    /**
     * 一般 - 数据有轻微缺失或延迟
     */
    FAIR(3, "一般"),

    /**
     * 较差 - 数据有明显缺失或延迟
     */
    POOR(2, "较差"),

    /**
     * 差 - 数据不可靠
     */
    BAD(1, "差"),

    /**
     * 未知 - 无法评估
     */
    UNKNOWN(0, "未知");

    private final int score;
    private final String displayName;

    DataQuality(int score, String displayName) {
        this.score = score;
        this.displayName = displayName;
    }

    public int getScore() {
        return score;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据分数获取质量等级
     */
    public static DataQuality fromScore(int score) {
        for (DataQuality quality : values()) {
            if (quality.score == score) {
                return quality;
            }
        }
        return UNKNOWN;
    }
}
