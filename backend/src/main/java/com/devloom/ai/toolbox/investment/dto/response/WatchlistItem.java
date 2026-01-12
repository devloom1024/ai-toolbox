package com.devloom.ai.toolbox.investment.dto.response;

import com.devloom.ai.toolbox.investment.domain.enums.Market;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * 自选股列表项
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItem {

    /**
     * 自选记录 ID，关联 t_watchlist.id
     */
    private Long id;

    /**
     * 股票/基金代码
     */
    private String symbol;

    /**
     * 市场类型
     */
    private Market market;

    /**
     * 股票/基金名称
     */
    private String name;

    /**
     * 当前价格/净值，单位：元 或 美元
     */
    private BigDecimal currentPrice;

    /**
     * 涨跌幅，保留 2 位小数，如 1.23 表示涨 1.23%
     */
    private BigDecimal changePercent;

    /**
     * AI 建仓建议
     */
    private Recommendation recommendation;

    /**
     * 置信度，0-100，表示 AI 建议的可信程度
     */
    private Integer confidence;

    /**
     * 分组 ID，关联 t_watchlist_group.id，null 表示未分组
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 添加时间
     */
    private OffsetDateTime addedAt;

    /**
     * AI 建仓建议枚举
     */
    public enum Recommendation {
        /**
         * 建仓推荐
         */
        BUILD_POSITION,

        /**
         * 持有
         */
        HOLD,

        /**
         * 观望
         */
        WAIT,

        /**
         * 回避
         */
        AVOID
    }
}
