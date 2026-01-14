package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Akshare Python 网关 HTTP 客户端。
 *
 * <p>封装对 Python Gateway 的 HTTP 调用。特定于 Akshare 数据源实现。</p>
 *
 * @author claude
 */
@Slf4j
class AksharePythonClient {

    private final AkshareProperties properties;
    private final RestClient restClient;

    AksharePythonClient(AkshareProperties properties, RestClient restClient) {
        this.properties = properties;
        this.restClient = restClient;
    }

    private String getGatewayUrl() {
        return properties.getGatewayUrl();
    }

    JsonNode search(String keyword, String market, int limit, int offset) {
        var params = new HashMap<String, String>();
        params.put("keyword", keyword);
        params.put("limit", String.valueOf(limit));
        if (market != null) {
            params.put("market", market);
        }
        String url = buildUrl("/api/v1/akshare/search", params);
        return getForObject(url);
    }

    JsonNode getQuote(String symbol, String market) {
        String url = buildUrl("/api/v1/akshare/quote/realtime", Map.of("symbol", symbol));
        return getForObject(url);
    }

    JsonNode getKline(String symbol, String period, String startDate, String endDate, int limit) {
        var params = new HashMap<String, String>();
        params.put("symbol", symbol);
        params.put("period", period);
        params.put("limit", String.valueOf(limit));
        if (startDate != null && !startDate.isEmpty()) {
            params.put("start_date", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            params.put("end_date", endDate);
        }
        String url = buildUrl("/api/v1/akshare/quote/kline", params);
        return getForObject(url);
    }

    JsonNode getCompanyInfo(String symbol) {
        String url = buildUrl("/api/v1/akshare/fundamental/company", Map.of("symbol", symbol));
        return getForObject(url);
    }

    JsonNode getFinancialIndicators(String symbol) {
        String url = buildUrl("/api/v1/akshare/fundamental/financial", Map.of("symbol", symbol));
        return getForObject(url);
    }

    JsonNode getCapitalFlow(String symbol) {
        String url = buildUrl("/api/v1/akshare/capital/flow", Map.of("symbol", symbol));
        return getForObject(url);
    }

    private String buildUrl(String path, Map<String, String> params) {
        StringBuilder url = new StringBuilder(getGatewayUrl());
        url.append(path);
        url.append("?");
        params.forEach((k, v) -> {
            if (v != null && !v.isEmpty()) {
                url.append(k).append("=").append(v).append("&");
            }
        });
        if (url.charAt(url.length() - 1) == '&') {
            url.deleteCharAt(url.length() - 1);
        }
        return url.toString();
    }

    private JsonNode getForObject(String url) {
        log.debug("Fetching data from: {}", url);
        try {
            return restClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.error("Market data API error: {} {}", response.getStatusCode(), response.getBody());
                    })
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            log.error("Failed to fetch market data: {}", e.getMessage());
            throw new MarketDataException("Failed to fetch market data: " + e.getMessage(), e);
        }
    }

    /**
     * 金融数据异常。
     */
    public static class MarketDataException extends RuntimeException {
        public MarketDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
