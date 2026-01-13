package com.devloom.ai.toolbox.investment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * K线数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KLineData {
    /**
     * 标的代码
     */
    private String symbol;

    /**
     * 时间
     */
    private LocalDateTime time;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 收盘价
     */
    private BigDecimal close;

    /**
     * 成交量 (股)
     */
    private Long volume;

    /**
     * 成交额 (元)
     */
    private BigDecimal amount;

    /**
     * 涨跌幅
     */
    private BigDecimal changeRate;

    /**
     * 换手率
     */
    private BigDecimal turnoverRate;

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return symbol != null
            && time != null
            && open != null && open.compareTo(BigDecimal.ZERO) > 0
            && high != null && high.compareTo(BigDecimal.ZERO) > 0
            && low != null && low.compareTo(BigDecimal.ZERO) > 0
            && close != null && close.compareTo(BigDecimal.ZERO) > 0
            && high.compareTo(low) >= 0;
    }

    /**
     * 计算涨跌幅
     */
    public void calculateChangeRate(BigDecimal preClose) {
        if (preClose != null && preClose.compareTo(BigDecimal.ZERO) > 0 && close != null) {
            this.changeRate = close.subtract(preClose)
                .divide(preClose, 4, BigDecimal.ROUND_HALF_UP);
        }
    }
}
