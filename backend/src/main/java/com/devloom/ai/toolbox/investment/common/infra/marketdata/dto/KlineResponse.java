package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * K 线响应。
 *
 * @author claude
 */
@Getter
@Builder
public class KlineResponse {

    /** 标的代码。 */
    private String symbol;

    /** K 线周期。 */
    private String period;

    /** K 线数据列表。 */
    private List<KlineItem> klines;

    @Getter
    @Builder
    public static class KlineItem {

        /** 时间戳。 */
        private LocalDateTime datetime;

        /** 开盘价。 */
        private BigDecimal open;

        /** 最高价。 */
        private BigDecimal high;

        /** 最低价。 */
        private BigDecimal low;

        /** 收盘价。 */
        private BigDecimal close;

        /** 成交量。 */
        private Long volume;

        /** 成交额。 */
        private BigDecimal turnover;

        /** 涨跌幅（%）。 */
        private BigDecimal changePercent;
    }
}
