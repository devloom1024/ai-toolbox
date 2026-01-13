package com.devloom.ai.toolbox.investment.infra.adapter.akshare;

import com.devloom.ai.toolbox.investment.domain.model.CapitalFlowData;
import com.devloom.ai.toolbox.investment.domain.model.FundamentalData;
import com.devloom.ai.toolbox.investment.domain.model.KLineData;
import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import com.devloom.ai.toolbox.investment.domain.model.StockSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Akshare HTTP客户端
 * <p>
 * 调用Python服务获取A股数据
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "investment.market-data.data-sources.a-share[0].enabled", havingValue = "true", matchIfMissing = true)
public class AkshareClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final int timeout;

    public AkshareClient(
        RestTemplate restTemplate,
        @Value("${investment.market-data.data-sources.a-share[0].url:http://localhost:8081}") String baseUrl,
        @Value("${investment.market-data.data-sources.a-share[0].timeout:5000}") int timeout
    ) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.timeout = timeout;
    }

    /**
     * 获取实时行情
     */
    public QuoteData getQuote(String symbol) {
        String url = baseUrl + "/api/quote?symbol=" + symbol;
        log.debug("Fetching quote from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                return mapToQuoteData(response.getBody());
            }
            return null;
        } catch (RestClientException e) {
            log.error("Failed to fetch quote for symbol: {}", symbol, e);
            throw new RuntimeException("Failed to fetch quote from Akshare", e);
        }
    }

    /**
     * 批量获取实时行情
     */
    @SuppressWarnings("unchecked")
    public List<QuoteData> getBatchQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        String url = baseUrl + "/api/quote/batch?symbols=" + String.join(",", symbols);
        log.debug("Fetching batch quotes from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof List) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) data;
                    List<QuoteData> quotes = new ArrayList<>();
                    for (Map<String, Object> item : list) {
                        QuoteData quote = mapToQuoteData(item);
                        if (quote != null) {
                            quotes.add(quote);
                        }
                    }
                    return quotes;
                }
            }
            return List.of();
        } catch (RestClientException e) {
            log.error("Failed to fetch batch quotes, count: {}", symbols.size(), e);
            throw new RuntimeException("Failed to fetch batch quotes from Akshare", e);
        }
    }

    /**
     * 获取K线数据
     */
    @SuppressWarnings("unchecked")
    public List<KLineData> getKLine(String symbol, String period, int limit) {
        String url = baseUrl + "/api/kline?symbol=" + symbol + "&period=" + period + "&limit=" + limit;
        log.debug("Fetching KLine from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof List) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) data;
                    List<KLineData> klines = new ArrayList<>();
                    for (Map<String, Object> item : list) {
                        KLineData kline = mapToKLineData(item, symbol);
                        if (kline != null) {
                            klines.add(kline);
                        }
                    }
                    return klines;
                }
            }
            return List.of();
        } catch (RestClientException e) {
            log.error("Failed to fetch KLine for symbol: {}", symbol, e);
            throw new RuntimeException("Failed to fetch KLine from Akshare", e);
        }
    }

    /**
     * 获取基本面数据
     */
    public FundamentalData getFundamental(String symbol) {
        String url = baseUrl + "/api/fundamental?symbol=" + symbol;
        log.debug("Fetching fundamental from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                return mapToFundamentalData(response.getBody());
            }
            return null;
        } catch (RestClientException e) {
            log.error("Failed to fetch fundamental for symbol: {}", symbol, e);
            throw new RuntimeException("Failed to fetch fundamental from Akshare", e);
        }
    }

    /**
     * 获取资金流向
     */
    public CapitalFlowData getCapitalFlow(String symbol) {
        String url = baseUrl + "/api/capital-flow?symbol=" + symbol;
        log.debug("Fetching capital flow from: {}", url);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                return mapToCapitalFlowData(response.getBody());
            }
            return null;
        } catch (RestClientException e) {
            log.error("Failed to fetch capital flow for symbol: {}", symbol, e);
            throw new RuntimeException("Failed to fetch capital flow from Akshare", e);
        }
    }

    /**
     * 健康检查
     */
    public boolean healthCheck() {
        try {
            String url = baseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );
            return response.getBody() != null && "ok".equals(response.getBody().get("status"));
        } catch (Exception e) {
            log.warn("Akshare health check failed", e);
            return false;
        }
    }

    /**
     * 搜索股票
     *
     * @param keyword 搜索关键字
     * @param market  市场类型 (可选，null 表示搜索所有市场)
     * @param limit   返回数量限制
     * @return 搜索结果列表
     */
    @SuppressWarnings("unchecked")
    public List<StockSearchResult> search(String keyword, com.devloom.ai.toolbox.investment.domain.enums.MarketType market, int limit) {
        try {
            // URL编码关键字
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            // 构建URL，不传 market 参数表示搜索所有市场
            StringBuilder urlBuilder = new StringBuilder(baseUrl + "/api/search?keyword=" + encodedKeyword + "&limit=" + limit);
            if (market != null) {
                urlBuilder.append("&market=").append(market.getCode());
            }

            String url = urlBuilder.toString();
            log.debug("Searching stocks from: {}", url);

            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );

            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof List) {
                    List<Map<String, Object>> list = (List<Map<String, Object>>) data;
                    List<StockSearchResult> results = new ArrayList<>();
                    for (Map<String, Object> item : list) {
                        StockSearchResult result = mapToSearchResult(item);
                        if (result != null) {
                            results.add(result);
                        }
                    }
                    return results;
                }
            }
            return List.of();
        } catch (RestClientException e) {
            log.error("Failed to search for keyword: {}", keyword, e);
            return List.of();
        }
    }

    /**
     * 映射响应到搜索结果
     */
    private StockSearchResult mapToSearchResult(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        try {
            String symbol = getString(response, "symbol");
            String name = getString(response, "name");

            if (symbol == null || name == null) {
                return null;
            }

            return StockSearchResult.builder()
                .symbol(symbol)
                .name(name)
                .build();
        } catch (Exception e) {
            log.error("Failed to map search result: {}", response, e);
            return null;
        }
    }

    /**
     * 获取热门股票列表 (用于缓存预热)
     */
    @SuppressWarnings("unchecked")
    public List<String> getPopularStocks(Object market) {
        try {
            String url = baseUrl + "/api/popular-stocks";
            ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.GET, createHttpEntity(), Map.class
            );
            if (response.getBody() != null) {
                Object data = response.getBody().get("data");
                if (data instanceof List) {
                    return (List<String>) data;
                }
            }
            return List.of();
        } catch (Exception e) {
            log.error("Failed to fetch popular stocks", e);
            return List.of();
        }
    }

    /**
     * 创建HTTP请求头
     */
    private HttpEntity<String> createHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "Spring-Boot-MarketDataClient");
        return new HttpEntity<>(headers);
    }

    /**
     * 映射响应到QuoteData
     */
    @SuppressWarnings("unchecked")
    private QuoteData mapToQuoteData(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        try {
            QuoteData.QuoteDataBuilder builder = QuoteData.builder()
                .symbol(getString(response, "symbol"))
                .name(getString(response, "name"))
                .price(getBigDecimal(response, "price"))
                .change(getBigDecimal(response, "change"))
                .changeRate(getBigDecimal(response, "change_rate"))
                .high(getBigDecimal(response, "high"))
                .low(getBigDecimal(response, "low"))
                .open(getBigDecimal(response, "open"))
                .preClose(getBigDecimal(response, "pre_close"))
                .volume(getLong(response, "volume"))
                .amount(getBigDecimal(response, "amount"))
                .turnoverRate(getBigDecimal(response, "turnover_rate"));

            // 时间戳处理
            String timestamp = getString(response, "timestamp");
            if (timestamp != null) {
                try {
                    builder.timestamp(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception e) {
                    builder.timestamp(LocalDateTime.now());
                }
            } else {
                builder.timestamp(LocalDateTime.now());
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to map quote data: {}", response, e);
            return null;
        }
    }

    /**
     * 映射响应到KLineData
     */
    @SuppressWarnings("unchecked")
    private KLineData mapToKLineData(Map<String, Object> response, String symbol) {
        if (response == null) {
            return null;
        }

        try {
            KLineData.KLineDataBuilder builder = KLineData.builder()
                .symbol(symbol)
                .open(getBigDecimal(response, "open"))
                .high(getBigDecimal(response, "high"))
                .low(getBigDecimal(response, "low"))
                .close(getBigDecimal(response, "close"))
                .volume(getLong(response, "volume"))
                .amount(getBigDecimal(response, "amount"));

            // 时间处理
            String timeStr = getString(response, "datetime");
            if (timeStr != null) {
                try {
                    builder.time(LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_DATE_TIME));
                } catch (Exception e) {
                    // 尝试日期格式
                    try {
                        builder.time(LocalDate.parse(timeStr, DateTimeFormatter.ISO_DATE).atStartOfDay());
                    } catch (Exception ex) {
                        builder.time(LocalDateTime.now());
                    }
                }
            } else {
                builder.time(LocalDateTime.now());
            }

            return builder.build();
        } catch (Exception e) {
            log.error("Failed to map KLine data: {}", response, e);
            return null;
        }
    }

    /**
     * 映射响应到FundamentalData
     */
    private FundamentalData mapToFundamentalData(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        try {
            return FundamentalData.builder()
                .symbol(getString(response, "symbol"))
                .name(getString(response, "name"))
                .peTtm(getBigDecimal(response, "pe_ttm"))
                .peStatic(getBigDecimal(response, "pe_static"))
                .pb(getBigDecimal(response, "pb"))
                .ps(getBigDecimal(response, "ps"))
                .roe(getBigDecimal(response, "roe"))
                .roa(getBigDecimal(response, "roa"))
                .grossProfitMargin(getBigDecimal(response, "gross_profit_margin"))
                .netProfitMargin(getBigDecimal(response, "net_profit_margin"))
                .eps(getBigDecimal(response, "eps"))
                .bps(getBigDecimal(response, "bps"))
                .totalMarketCap(getBigDecimal(response, "total_market_cap"))
                .circulationMarketCap(getBigDecimal(response, "circulation_market_cap"))
                .totalShares(getBigDecimal(response, "total_shares"))
                .circulationShares(getBigDecimal(response, "circulation_shares"))
                .revenue(getBigDecimal(response, "revenue"))
                .netProfit(getBigDecimal(response, "net_profit"))
                .revenueGrowthRate(getBigDecimal(response, "revenue_growth_rate"))
                .netProfitGrowthRate(getBigDecimal(response, "net_profit_growth_rate"))
                .dataDate(LocalDate.now())
                .build();
        } catch (Exception e) {
            log.error("Failed to map fundamental data: {}", response, e);
            return null;
        }
    }

    /**
     * 映射响应到CapitalFlowData
     */
    private CapitalFlowData mapToCapitalFlowData(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        try {
            return CapitalFlowData.builder()
                .symbol(getString(response, "symbol"))
                .name(getString(response, "name"))
                .date(LocalDate.now())
                .mainNetInflow(getBigDecimal(response, "main_net_inflow"))
                .mainNetInflowRate(getBigDecimal(response, "main_net_inflow_rate"))
                .superLargeNetInflow(getBigDecimal(response, "super_large_net_inflow"))
                .largeNetInflow(getBigDecimal(response, "large_net_inflow"))
                .mediumNetInflow(getBigDecimal(response, "medium_net_inflow"))
                .smallNetInflow(getBigDecimal(response, "small_net_inflow"))
                .northboundNetInflow(getBigDecimal(response, "northbound_net_inflow"))
                .build();
        } catch (Exception e) {
            log.error("Failed to map capital flow data: {}", response, e);
            return null;
        }
    }

    // ========== 辅助方法 ==========

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private BigDecimal getBigDecimal(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            }
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal for key: {}, value: {}", key, value);
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
            log.warn("Failed to parse Long for key: {}, value: {}", key, value);
            return null;
        }
    }
}
