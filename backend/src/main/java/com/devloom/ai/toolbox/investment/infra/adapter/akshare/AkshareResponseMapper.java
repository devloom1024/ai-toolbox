package com.devloom.ai.toolbox.investment.infra.adapter.akshare;

import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import com.devloom.ai.toolbox.investment.domain.model.KLineData;
import com.devloom.ai.toolbox.investment.infra.quality.DataCleaner;
import com.devloom.ai.toolbox.investment.infra.quality.DataQualityValidator;
import com.devloom.ai.toolbox.investment.infra.quality.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Akshare响应映射器
 * <p>
 * 负责将Python服务的响应转换为统一的数据模型
 */
@Slf4j
@Component
public class AkshareResponseMapper {

    private final DataCleaner dataCleaner;
    private final DataQualityValidator qualityValidator;

    public AkshareResponseMapper(DataCleaner dataCleaner, DataQualityValidator qualityValidator) {
        this.dataCleaner = dataCleaner;
        this.qualityValidator = qualityValidator;
    }

    /**
     * 映射并清洗QuoteData列表
     */
    public List<QuoteData> mapAndCleanQuotes(List<Map<String, Object>> rawDataList) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return List.of();
        }

        return rawDataList.stream()
            .map(this::mapToQuoteData)
            .map(dataCleaner::cleanQuote)
            .filter(this::isAcceptableQuality)
            .collect(Collectors.toList());
    }

    /**
     * 映射并清洗KLineData列表
     */
    public List<KLineData> mapAndCleanKLines(List<Map<String, Object>> rawDataList, String symbol) {
        if (rawDataList == null || rawDataList.isEmpty()) {
            return List.of();
        }

        return rawDataList.stream()
            .map(data -> mapToKLineData(data, symbol))
            .map(dataCleaner::nullifyInvalidValues)
            .filter(kline -> {
                ValidationResult result = qualityValidator.validateKLine(kline);
                return result.isValid();
            })
            .collect(Collectors.toList());
    }

    /**
     * 映射QuoteData
     */
    private QuoteData mapToQuoteData(Map<String, Object> raw) {
        if (raw == null) {
            return null;
        }

        try {
            return QuoteData.builder()
                .symbol(getString(raw, "symbol"))
                .name(getString(raw, "name"))
                .price(getBigDecimal(raw, "price"))
                .change(getBigDecimal(raw, "change"))
                .changeRate(getBigDecimal(raw, "change_rate"))
                .high(getBigDecimal(raw, "high"))
                .low(getBigDecimal(raw, "low"))
                .open(getBigDecimal(raw, "open"))
                .preClose(getBigDecimal(raw, "pre_close"))
                .volume(getLong(raw, "volume"))
                .amount(getBigDecimal(raw, "amount"))
                .turnoverRate(getBigDecimal(raw, "turnover_rate"))
                .pe(getBigDecimal(raw, "pe"))
                .pb(getBigDecimal(raw, "pb"))
                .build();
        } catch (Exception e) {
            log.warn("Failed to map quote data: {}", raw, e);
            return null;
        }
    }

    /**
     * 映射KLineData
     */
    private KLineData mapToKLineData(Map<String, Object> raw, String symbol) {
        if (raw == null) {
            return null;
        }

        try {
            return KLineData.builder()
                .symbol(symbol)
                .open(getBigDecimal(raw, "open"))
                .high(getBigDecimal(raw, "high"))
                .low(getBigDecimal(raw, "low"))
                .close(getBigDecimal(raw, "close"))
                .volume(getLong(raw, "volume"))
                .amount(getBigDecimal(raw, "amount"))
                .build();
        } catch (Exception e) {
            log.warn("Failed to map KLine data: {}", raw, e);
            return null;
        }
    }

    /**
     * 检查数据质量是否可接受
     */
    private boolean isAcceptableQuality(QuoteData data) {
        if (data == null) {
            return false;
        }
        ValidationResult result = qualityValidator.validateQuote(data);
        // 警告级别可以接受,但错误级别不可以
        return result.isValid();
    }

    // ========== 辅助方法 ==========

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private java.math.BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return java.math.BigDecimal.valueOf(((Number) value).doubleValue());
            }
            return new java.math.BigDecimal(value.toString().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            return Long.parseLong(value.toString().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
