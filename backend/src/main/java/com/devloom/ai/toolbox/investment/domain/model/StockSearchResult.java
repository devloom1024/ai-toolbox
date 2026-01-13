package com.devloom.ai.toolbox.investment.domain.model;

import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 股票搜索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSearchResult {

    /**
     * 股票代码
     */
    private String symbol;

    /**
     * 股票名称
     */
    private String name;

    /**
     * 市场类型
     */
    private MarketType market;

    /**
     * 拼音缩写
     */
    private String pinyin;

    /**
     * 交易所 (SH/SZ/BJ)
     */
    private String exchange;

    /**
     * 当前价 (可选,用于展示)
     */
    private BigDecimal price;

    /**
     * 涨跌幅 (可选)
     */
    private BigDecimal changeRate;

    /**
     * 匹配类型
     */
    private MatchType matchType;

    /**
     * 匹配得分 (用于排序)
     */
    private Double score;

    /**
     * 匹配类型枚举
     */
    public enum MatchType {
        /**
         * 代码匹配
         */
        CODE,

        /**
         * 名称匹配
         */
        NAME,

        /**
         * 拼音匹配
         */
        PINYIN,

        /**
         * 模糊匹配
         */
        FUZZY
    }

    /**
     * 获取唯一标识
     */
    public String getUniqueKey() {
        return market.getCode() + ":" + symbol;
    }

    /**
     * 计算匹配得分
     *
     * @param keyword 搜索关键字
     * @return 匹配得分
     */
    public double calculateMatchScore(String keyword) {
        String lowerKeyword = keyword.toLowerCase().trim();
        String lowerSymbol = symbol != null ? symbol.toLowerCase() : "";
        String lowerName = name != null ? name.toLowerCase() : "";
        String lowerPinyin = pinyin != null ? pinyin.toLowerCase() : "";

        double score = 0.0;

        // 代码完全匹配 (最高优先级)
        if (lowerSymbol.equals(lowerKeyword)) {
            score = 100.0;
            this.matchType = MatchType.CODE;
        }
        // 代码前缀匹配
        else if (lowerSymbol.startsWith(lowerKeyword)) {
            score = 90.0;
            this.matchType = MatchType.CODE;
        }
        // 拼音缩写匹配
        else if (lowerPinyin.startsWith(lowerKeyword)) {
            score = 80.0;
            this.matchType = MatchType.PINYIN;
        }
        // 名称包含关键字
        else if (lowerName.contains(lowerKeyword)) {
            score = 70.0;
            this.matchType = MatchType.NAME;
        }
        // 拼音包含
        else if (lowerPinyin.contains(lowerKeyword)) {
            score = 60.0;
            this.matchType = MatchType.PINYIN;
        }
        // 模糊匹配
        else {
            score = calculateFuzzyScore(lowerKeyword, lowerSymbol + lowerName + lowerPinyin);
            this.matchType = MatchType.FUZZY;
        }

        this.score = score;
        return score;
    }

    /**
     * 计算模糊匹配得分
     */
    private double calculateFuzzyScore(String keyword, String text) {
        if (keyword.isEmpty() || text.isEmpty()) {
            return 0.0;
        }

        // 简单计算: 字符重叠率
        int matchCount = 0;
        for (char c : keyword.toCharArray()) {
            if (text.indexOf(c) >= 0) {
                matchCount++;
            }
        }

        double ratio = (double) matchCount / keyword.length();
        return ratio * 50.0; // 最高50分
    }

    /**
     * 检查是否匹配关键字
     */
    public boolean matches(String keyword) {
        String lowerKeyword = keyword.toLowerCase().trim();

        return Objects.equals(symbol, lowerKeyword)
            || (name != null && name.toLowerCase().contains(lowerKeyword))
            || (pinyin != null && pinyin.toLowerCase().contains(lowerKeyword));
    }

    /**
     * 创建搜索结果 (工厂方法)
     */
    public static StockSearchResult of(String symbol, String name, MarketType market) {
        return StockSearchResult.builder()
            .symbol(symbol)
            .name(name)
            .market(market)
            .build();
    }

    /**
     * 创建带价格的搜索结果
     */
    public static StockSearchResult withPrice(String symbol, String name, MarketType market,
                                               BigDecimal price, BigDecimal changeRate) {
        return StockSearchResult.builder()
            .symbol(symbol)
            .name(name)
            .market(market)
            .price(price)
            .changeRate(changeRate)
            .build();
    }
}
