package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * K 线结果。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class KlineResult {

    /** 标的代码。 */
    private String symbol;

    /** K 线周期。 */
    private String period;

    /** K 线数据列表。 */
    private List<KlineItem> klines;

    @Getter
    @SuperBuilder
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
