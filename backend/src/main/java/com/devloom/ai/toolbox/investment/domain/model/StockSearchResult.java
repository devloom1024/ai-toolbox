package com.devloom.ai.toolbox.investment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * 创建搜索结果 (工厂方法)
     */
    public static StockSearchResult of(String symbol, String name) {
        return StockSearchResult.builder()
            .symbol(symbol)
            .name(name)
            .build();
    }
}
