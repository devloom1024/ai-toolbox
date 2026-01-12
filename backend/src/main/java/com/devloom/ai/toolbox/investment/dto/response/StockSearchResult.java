package com.devloom.ai.toolbox.investment.dto.response;

import com.devloom.ai.toolbox.investment.domain.enums.Market;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 股票搜索结果项
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockSearchResult {

    /**
     * 股票/基金代码
     */
    private String symbol;

    /**
     * 股票/基金名称
     */
    private String name;

    /**
     * 市场类型
     */
    private Market market;

    /**
     * 标的类型
     */
    private SecurityType type;

    /**
     * 完整代码（含市场前缀，如 SH600519）
     */
    private String fullCode;

    /**
     * 拼音简拼
     */
    private String pinyin;

    /**
     * 所属行业
     */
    private String industry;

    /**
     * 标的安全类型枚举
     */
    public enum SecurityType {
        /**
         * 股票
         */
        STOCK,

        /**
         * ETF 交易所交易基金
         */
        ETF,

        /**
         * 场外基金
         */
        FUND,

        /**
         * 指数
         */
        INDEX
    }
}
