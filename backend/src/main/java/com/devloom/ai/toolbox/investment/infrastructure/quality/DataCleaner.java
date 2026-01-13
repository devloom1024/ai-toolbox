package com.devloom.ai.toolbox.investment.infrastructure.quality;

import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 数据清洗器
 * <p>
 * 负责清洗和标准化数据格式
 */
@Slf4j
@Component
public class DataCleaner {

    /**
     * 价格精度 (小数位数)
     */
    private static final int PRICE_SCALE = 2;

    /**
     * 百分比精度 (小数位数)
     */
    private static final int PERCENTAGE_SCALE = 4;

    /**
     * 金额精度 (小数位数)
     */
    private static final int AMOUNT_SCALE = 2;

    /**
     * 清洗行情数据
     *
     * @param raw 原始数据
     * @return 清洗后的数据
     */
    public QuoteData cleanQuote(QuoteData raw) {
        if (raw == null) {
            return null;
        }

        QuoteData cleaned = new QuoteData();
        cleaned.setSymbol(cleanSymbol(raw.getSymbol()));
        cleaned.setName(cleanName(raw.getName()));
        cleaned.setMarket(raw.getMarket());
        cleaned.setSource(raw.getSource());

        // 价格类数据 - 2位小数
        cleaned.setPrice(scalePrice(raw.getPrice()));
        cleaned.setChange(scalePrice(raw.getChange()));
        cleaned.setChangeRate(scalePercentage(raw.getChangeRate()));
        cleaned.setHigh(scalePrice(raw.getHigh()));
        cleaned.setLow(scalePrice(raw.getLow()));
        cleaned.setOpen(scalePrice(raw.getOpen()));
        cleaned.setPreClose(scalePrice(raw.getPreClose()));

        // 整数类数据
        cleaned.setVolume(raw.getVolume());

        // 金额类数据 - 2位小数
        cleaned.setAmount(scaleAmount(raw.getAmount()));

        // 百分比类数据 - 4位小数
        cleaned.setTurnoverRate(scalePercentage(raw.getTurnoverRate()));
        cleaned.setPe(scalePercentage(raw.getPe()));
        cleaned.setPb(scalePercentage(raw.getPb()));

        // 大数值类数据
        cleaned.setTotalMarketCap(scaleAmount(raw.getTotalMarketCap()));
        cleaned.setCirculationMarketCap(scaleAmount(raw.getCirculationMarketCap()));

        // 时间戳
        cleaned.setTimestamp(raw.getTimestamp());

        // 布尔类
        cleaned.setTrading(raw.getTrading());

        return cleaned;
    }

    /**
     * 清洗标的代码
     */
    private String cleanSymbol(String symbol) {
        if (symbol == null) {
            return null;
        }
        // 去除空格,统一大写
        return symbol.trim().toUpperCase();
    }

    /**
     * 清洗名称
     */
    private String cleanName(String name) {
        if (name == null) {
            return null;
        }
        // 去除首尾空格
        return name.trim();
    }

    /**
     * 价格精度处理
     */
    private BigDecimal scalePrice(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 金额精度处理
     */
    private BigDecimal scaleAmount(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 百分比精度处理
     */
    private BigDecimal scalePercentage(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(PERCENTAGE_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 处理空值
     */
    public QuoteData nullifyInvalidValues(QuoteData data) {
        if (data == null) {
            return null;
        }

        // 价格不能为0或负数
        if (data.getPrice() != null && data.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            data.setPrice(null);
        }

        // 成交量不能为负数
        if (data.getVolume() != null && data.getVolume() < 0) {
            data.setVolume(0L);
        }

        // 涨跌幅超过范围的处理
        if (data.getChangeRate() != null) {
            BigDecimal abs = data.getChangeRate().abs();
            if (abs.compareTo(new BigDecimal("10")) > 0) {
                log.warn("ChangeRate %.2f is abnormal, set to null", data.getChangeRate());
                data.setChangeRate(null);
            }
        }

        return data;
    }

    /**
     * 补充缺失字段
     */
    public QuoteData fillMissingFields(QuoteData data, QuoteData fallback) {
        if (data == null) {
            return fallback;
        }

        if (data.getName() == null && fallback != null) {
            data.setName(fallback.getName());
        }

        if (data.getPreClose() == null && fallback != null && data.getOpen() != null) {
            // 估算昨收价
            if (data.getChangeRate() != null) {
                data.setPreClose(data.getOpen()
                    .divide(data.getChangeRate().add(BigDecimal.ONE), 2, RoundingMode.HALF_UP));
            }
        }

        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }

        return data;
    }
}
