package com.devloom.ai.toolbox.investment.domain.model;

import com.devloom.ai.toolbox.investment.domain.enums.DataQuality;
import com.devloom.ai.toolbox.investment.domain.enums.DataSourceType;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实时行情数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteData {
    /**
     * 标的代码
     */
    private String symbol;

    /**
     * 标的名称
     */
    private String name;

    /**
     * 市场类型
     */
    private MarketType market;

    /**
     * 最新价
     */
    private BigDecimal price;

    /**
     * 涨跌额
     */
    private BigDecimal change;

    /**
     * 涨跌幅 (小数形式, 如 0.05 表示 5%)
     */
    private BigDecimal changeRate;

    /**
     * 最高价
     */
    private BigDecimal high;

    /**
     * 最低价
     */
    private BigDecimal low;

    /**
     * 开盘价
     */
    private BigDecimal open;

    /**
     * 昨收价
     */
    private BigDecimal preClose;

    /**
     * 成交量 (股)
     */
    private Long volume;

    /**
     * 成交额 (元)
     */
    private BigDecimal amount;

    /**
     * 换手率
     */
    private BigDecimal turnoverRate;

    /**
     * 市盈率 (动态)
     */
    private BigDecimal pe;

    /**
     * 市净率
     */
    private BigDecimal pb;

    /**
     * 总市值 (元)
     */
    private BigDecimal totalMarketCap;

    /**
     * 流通市值 (元)
     */
    private BigDecimal circulationMarketCap;

    /**
     * 数据时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 数据来源
     */
    private DataSourceType source;

    /**
     * 数据质量评分
     */
    private DataQuality quality;

    /**
     * 是否交易中
     */
    private Boolean trading;

    /**
     * 检查数据是否有效
     */
    public boolean isValid() {
        return symbol != null
            && price != null
            && price.compareTo(BigDecimal.ZERO) > 0
            && timestamp != null;
    }

    /**
     * 获取数据年龄(分钟)
     */
    public long getAgeInMinutes() {
        if (timestamp == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(timestamp, LocalDateTime.now()).toMinutes();
    }
}
