package com.devloom.ai.toolbox.investment.infrastructure.quality;

import com.devloom.ai.toolbox.investment.domain.enums.DataQuality;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.FundamentalData;
import com.devloom.ai.toolbox.investment.domain.model.KLineData;
import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据质量校验器
 * <p>
 * 负责校验数据的完整性和合理性
 */
@Slf4j
@Component
public class DataQualityValidator {

    /**
     * A股涨跌停限制 (日常)
     */
    private static final BigDecimal A_SHARE_DAILY_LIMIT = new BigDecimal("0.10");

    /**
     * ST股票涨跌停限制
     */
    private static final BigDecimal ST_DAILY_LIMIT = new BigDecimal("0.05");

    /**
     * 科创板/创业板涨跌停限制
     */
    private static final BigDecimal CHANGER_DAILY_LIMIT = new BigDecimal("0.20");

    /**
     * 数据最大允许延迟(分钟)
     */
    private static final long MAX_DATA_AGE_MINUTES = 30;

    /**
     * 价格合理范围 (1元 - 100万元)
     */
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000");

    /**
     * 校验行情数据
     *
     * @param quote 行情数据
     * @return 校验结果
     */
    public ValidationResult validateQuote(QuoteData quote) {
        ValidationResult result = new ValidationResult();

        if (quote == null) {
            result.addError("行情数据为空");
            return result;
        }

        // 1. 必填字段校验
        validateRequiredFields(quote, result);

        // 2. 价格范围校验
        validatePriceRange(quote, result);

        // 3. 价格关系校验 (开盘价、最高价、最低价、收盘价)
        validatePriceRelations(quote, result);

        // 4. 涨跌幅合理性校验
        validateChangeRate(quote, result);

        // 5. 成交量校验
        validateVolume(quote, result);

        // 6. 数据新鲜度校验
        validateTimestamp(quote, result);

        // 7. 计算数据质量评分
        result.setQuality(calculateQuality(quote, result));

        return result;
    }

    /**
     * 校验K线数据
     */
    public ValidationResult validateKLine(KLineData kline) {
        ValidationResult result = new ValidationResult();

        if (kline == null) {
            result.addError("K线数据为空");
            return result;
        }

        // 1. 价格关系校验
        if (kline.getHigh() != null && kline.getLow() != null) {
            if (kline.getHigh().compareTo(kline.getLow()) < 0) {
                result.addError(String.format(
                    "K线价格关系错误: 最高价(%.2f) < 最低价(%.2f)",
                    kline.getHigh(), kline.getLow()
                ));
            }
        }

        // 2. 开盘价和收盘价应该在高低价范围内
        if (kline.getOpen() != null && kline.getHigh() != null) {
            if (kline.getOpen().compareTo(kline.getHigh()) > 0) {
                result.addError(String.format(
                    "开盘价(%.2f) > 最高价(%.2f)",
                    kline.getOpen(), kline.getHigh()
                ));
            }
        }

        if (kline.getOpen() != null && kline.getLow() != null) {
            if (kline.getOpen().compareTo(kline.getLow()) < 0) {
                result.addError(String.format(
                    "开盘价(%.2f) < 最低价(%.2f)",
                    kline.getOpen(), kline.getLow()
                ));
            }
        }

        // 3. 计算质量评分
        result.setQuality(calculateKLineQuality(result));

        return result;
    }

    /**
     * 校验基本面数据
     */
    public ValidationResult validateFundamental(FundamentalData fundamental) {
        ValidationResult result = new ValidationResult();

        if (fundamental == null) {
            result.addError("基本面数据为空");
            return result;
        }

        // 1. 必填字段校验
        if (fundamental.getSymbol() == null || fundamental.getSymbol().isEmpty()) {
            result.addError("标的代码为空");
        }

        // 2. 估值指标范围校验
        validateValuationMetrics(fundamental, result);

        // 3. 质量评分
        result.setQuality(calculateFundamentalQuality(fundamental, result));

        return result;
    }

    /**
     * 校验必填字段
     */
    private void validateRequiredFields(QuoteData quote, ValidationResult result) {
        List<String> missingFields = new ArrayList<>();

        if (quote.getSymbol() == null || quote.getSymbol().isEmpty()) {
            missingFields.add("symbol");
        }
        if (quote.getPrice() == null) {
            missingFields.add("price");
        }
        if (quote.getTimestamp() == null) {
            missingFields.add("timestamp");
        }

        if (!missingFields.isEmpty()) {
            result.addError("缺少必填字段: " + String.join(", ", missingFields));
        }
    }

    /**
     * 校验价格范围
     */
    private void validatePriceRange(QuoteData quote, ValidationResult result) {
        if (quote.getPrice() != null) {
            if (quote.getPrice().compareTo(MIN_PRICE) < 0) {
                result.addError(String.format("价格%.2f低于最小允许值%.2f",
                    quote.getPrice(), MIN_PRICE));
            }
            if (quote.getPrice().compareTo(MAX_PRICE) > 0) {
                result.addWarning(String.format("价格%.2f高于正常范围,可能为异常数据",
                    quote.getPrice()));
            }
        }
    }

    /**
     * 校验价格关系
     */
    private void validatePriceRelations(QuoteData quote, ValidationResult result) {
        // 检查最高价和最低价
        if (quote.getHigh() != null && quote.getLow() != null) {
            if (quote.getHigh().compareTo(quote.getLow()) < 0) {
                result.addError(String.format(
                    "最高价(%.2f) < 最低价(%.2f)",
                    quote.getHigh(), quote.getLow()
                ));
            }
        }

        // 检查开盘价和昨收价
        if (quote.getOpen() != null && quote.getPreClose() != null) {
            BigDecimal openChange = quote.getOpen()
                .subtract(quote.getPreClose())
                .divide(quote.getPreClose(), 4, RoundingMode.HALF_UP);

            // 开盘涨跌幅超过9%视为异常
            if (openChange.abs().compareTo(new BigDecimal("0.09")) > 0) {
                result.addWarning(String.format(
                    "开盘涨跌幅%.2f%%异常,可能为复牌或除权",
                    openChange.multiply(new BigDecimal("100"))
                ));
            }
        }
    }

    /**
     * 校验涨跌幅合理性
     */
    private void validateChangeRate(QuoteData quote, ValidationResult result) {
        if (quote.getChangeRate() == null) {
            return;
        }

        BigDecimal changeRate = quote.getChangeRate().abs();

        // A股涨跌停限制校验
        if (quote.getMarket() == MarketType.A_SHARE) {
            // 判断是否为ST股票 (简单判断:名称包含ST)
            boolean isSt = quote.getName() != null && quote.getName().contains("ST");

            BigDecimal limit = isSt ? ST_DAILY_LIMIT : A_SHARE_DAILY_LIMIT;

            if (changeRate.compareTo(limit) > 0) {
                // 涨停
                if (quote.getChangeRate().compareTo(BigDecimal.ZERO) > 0) {
                    result.addWarning(String.format(
                        "涨幅%.2f%%超过%.0f%%限制,可能为涨停股票",
                        changeRate.multiply(new BigDecimal("100")),
                        limit.multiply(new BigDecimal("100"))
                    ));
                }
                // 跌停
                else {
                    result.addWarning(String.format(
                        "跌幅%.2f%%超过%.0f%%限制,可能为跌停股票",
                        changeRate.multiply(new BigDecimal("100")),
                        limit.multiply(new BigDecimal("100"))
                    ));
                }
            }
        }
    }

    /**
     * 校验成交量
     */
    private void validateVolume(QuoteData quote, ValidationResult result) {
        if (quote.getVolume() != null) {
            if (quote.getVolume() < 0) {
                result.addError("成交量不能为负数");
            }
            if (quote.getVolume() == 0 && quote.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                result.addWarning("零成交量的有效价格数据");
            }
        }
    }

    /**
     * 校验数据时间戳
     */
    private void validateTimestamp(QuoteData quote, ValidationResult result) {
        if (quote.getTimestamp() == null) {
            result.addError("数据时间戳为空");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration age = Duration.between(quote.getTimestamp(), now);
        long minutes = age.toMinutes();

        if (minutes > MAX_DATA_AGE_MINUTES) {
            result.addWarning(String.format(
                "数据延迟%d分钟,超过最大允许延迟%d分钟",
                minutes, MAX_DATA_AGE_MINUTES
            ));
        }

        // 数据时间不能在未来
        if (age.isNegative()) {
            result.addError("数据时间戳在将来,可能是系统时间错误");
        }
    }

    /**
     * 校验估值指标
     */
    private void validateValuationMetrics(FundamentalData fundamental, ValidationResult result) {
        // PE校验
        if (fundamental.getPeTtm() != null) {
            if (fundamental.getPeTtm().compareTo(BigDecimal.ZERO) < 0) {
                result.addWarning("PE为负数,公司可能亏损");
            }
            if (fundamental.getPeTtm().compareTo(new BigDecimal("500")) > 0) {
                result.addWarning(String.format("PE=%.2f过高,可能为异常数据",
                    fundamental.getPeTtm()));
            }
        }

        // PB校验
        if (fundamental.getPb() != null) {
            if (fundamental.getPb().compareTo(BigDecimal.ZERO) < 0) {
                result.addError("PB不能为负数");
            }
            if (fundamental.getPb().compareTo(new BigDecimal("50")) > 0) {
                result.addWarning(String.format("PB=%.2f过高", fundamental.getPb()));
            }
        }

        // ROE校验
        if (fundamental.getRoe() != null) {
            if (fundamental.getRoe().compareTo(new BigDecimal("100")) > 0) {
                result.addWarning(String.format("ROE=%.2f%%超过100%%,可能为异常数据",
                    fundamental.getRoe()));
            }
        }
    }

    /**
     * 计算行情数据质量评分
     */
    private DataQuality calculateQuality(QuoteData quote, ValidationResult result) {
        int errorCount = result.getErrors().size();
        int warningCount = result.getWarnings().size();

        if (errorCount > 0) {
            return DataQuality.BAD;
        }

        if (warningCount >= 3) {
            return DataQuality.POOR;
        }

        if (warningCount >= 2) {
            return DataQuality.FAIR;
        }

        if (warningCount == 1) {
            return DataQuality.GOOD;
        }

        // 检查数据新鲜度
        if (quote.getTimestamp() != null) {
            long age = Duration.between(quote.getTimestamp(), LocalDateTime.now()).toMinutes();
            if (age > 5) {
                return DataQuality.FAIR;
            }
        }

        return DataQuality.EXCELLENT;
    }

    /**
     * 计算K线数据质量评分
     */
    private DataQuality calculateKLineQuality(ValidationResult result) {
        if (!result.getErrors().isEmpty()) {
            return DataQuality.BAD;
        }
        if (!result.getWarnings().isEmpty()) {
            return DataQuality.FAIR;
        }
        return DataQuality.EXCELLENT;
    }

    /**
     * 计算基本面数据质量评分
     */
    private DataQuality calculateFundamentalQuality(FundamentalData fundamental, ValidationResult result) {
        if (!result.getErrors().isEmpty()) {
            return DataQuality.BAD;
        }

        // 统计有多少关键指标
        int validMetrics = 0;
        int totalMetrics = 8; // PE, PB, ROE, EPS, 营收, 净利润, 总市值, 流通市值

        if (fundamental.getPeTtm() != null || fundamental.getPeStatic() != null) validMetrics++;
        if (fundamental.getPb() != null) validMetrics++;
        if (fundamental.getRoe() != null) validMetrics++;
        if (fundamental.getEps() != null) validMetrics++;
        if (fundamental.getRevenue() != null) validMetrics++;
        if (fundamental.getNetProfit() != null) validMetrics++;
        if (fundamental.getTotalMarketCap() != null) validMetrics++;
        if (fundamental.getCirculationMarketCap() != null) validMetrics++;

        double completeness = (double) validMetrics / totalMetrics;

        if (completeness >= 0.8) {
            return DataQuality.EXCELLENT;
        } else if (completeness >= 0.6) {
            return DataQuality.GOOD;
        } else if (completeness >= 0.4) {
            return DataQuality.FAIR;
        } else {
            return DataQuality.POOR;
        }
    }
}
