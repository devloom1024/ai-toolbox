package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Akshare 数据源适配器。
 *
 * @author claude
 */
@Slf4j
@Component
public class AkshareAdapter implements MarketDataAdapter {

    private final AksharePythonClient pythonClient;

    @Autowired
    public AkshareAdapter(AkshareProperties properties, RestClient akshareRestClient) {
        this.pythonClient = new AksharePythonClient(properties, akshareRestClient);
    }

    @Override
    public String getName() {
        return "akshare";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isAvailable() {
        try {
            pythonClient.search("test", null, 1, 0);
            return true;
        } catch (Exception e) {
            log.warn("Akshare health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<SecuritySearchResult> search(SearchRequest request) {
        try {
            JsonNode rawData = pythonClient.search(
                    request.getKeyword(),
                    request.getMarket(),
                    request.getLimit(),
                    request.getOffset()
            );
            return parseSearchResults(rawData);
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare search failed: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public QuoteResponse getQuote(String symbol, String market) {
        try {
            JsonNode rawData = pythonClient.getQuote(symbol, market);
            return parseQuoteResponse(rawData);
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare quote failed for {}: {}", symbol, e.getMessage());
            throw e;
        }
    }

    @Override
    public KlineResponse getKline(KlineRequest request) {
        try {
            JsonNode rawData = pythonClient.getKline(
                    request.getSymbol(),
                    request.getPeriod(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getLimit()
            );
            return parseKlineResponse(rawData, request.getPeriod());
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare kline failed for {}: {}", request.getSymbol(), e.getMessage());
            throw e;
        }
    }

    @Override
    public FundamentalResponse getFundamental(String symbol, String market) {
        try {
            JsonNode rawData = pythonClient.getCompanyInfo(symbol);
            return parseFundamentalResponse(rawData);
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare fundamental failed for {}: {}", symbol, e.getMessage());
            throw e;
        }
    }

    @Override
    public FinancialResponse getFinancial(String symbol, String market) {
        try {
            JsonNode rawData = pythonClient.getFinancialIndicators(symbol);
            return parseFinancialResponse(rawData);
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare financial failed for {}: {}", symbol, e.getMessage());
            throw e;
        }
    }

    @Override
    public CapitalFlowResponse getCapitalFlow(String symbol, String market) {
        try {
            JsonNode rawData = pythonClient.getCapitalFlow(symbol);
            return parseCapitalFlowResponse(rawData);
        } catch (AksharePythonClient.MarketDataException e) {
            log.warn("Akshare capital flow failed for {}: {}", symbol, e.getMessage());
            throw e;
        }
    }

    // ========== 内部解析方法 ==========

    private List<SecuritySearchResult> parseSearchResults(JsonNode data) {
        List<SecuritySearchResult> results = new ArrayList<>();

        if (data == null || !data.has("data")) {
            return results;
        }

        ArrayNode items = (ArrayNode) data.get("data");
        for (JsonNode item : items) {
            SecuritySearchResult result = SecuritySearchResult.builder()
                    .symbol(getText(item, "symbol"))
                    .name(getText(item, "name"))
                    .market(getText(item, "market"))
                    .type(getText(item, "type"))
                    .build();
            results.add(result);
        }

        return results;
    }

    private QuoteResponse parseQuoteResponse(JsonNode data) {
        if (data == null || !data.has("data")) {
            throw new AksharePythonClient.MarketDataException("Invalid quote response format", null);
        }

        JsonNode items = data.get("data");
        if (items == null || !items.isArray() || items.size() == 0) {
            throw new AksharePythonClient.MarketDataException("Empty quote data", null);
        }

        JsonNode item = items.get(0);
        return QuoteResponse.builder()
                .symbol(getText(item, "symbol"))
                .name(getText(item, "name"))
                .currentPrice(getDecimal(item, "currentPrice"))
                .openPrice(getDecimal(item, "openPrice"))
                .highPrice(getDecimal(item, "highPrice"))
                .lowPrice(getDecimal(item, "lowPrice"))
                .preClosePrice(getDecimal(item, "preClosePrice"))
                .change(getDecimal(item, "change"))
                .changePercent(getDecimal(item, "changePercent"))
                .volume(getLong(item, "volume"))
                .turnover(getDecimal(item, "turnover"))
                .turnoverRate(getDecimal(item, "turnoverRate"))
                .amplitude(getDecimal(item, "amplitude"))
                .marketCap(getDecimal(item, "marketCap"))
                .circulatingMarketCap(getDecimal(item, "circulatingMarketCap"))
                .peRatio(getDecimal(item, "peRatio"))
                .pbRatio(getDecimal(item, "pbRatio"))
                .dividendYield(getDecimal(item, "dividendYield"))
                .lastUpdated(Instant.now())
                .build();
    }

    private KlineResponse parseKlineResponse(JsonNode data, String period) {
        List<KlineResponse.KlineItem> klines = new ArrayList<>();

        if (data == null || !data.has("data")) {
            return KlineResponse.builder()
                    .symbol(null)
                    .period(period)
                    .klines(klines)
                    .build();
        }

        ArrayNode items = (ArrayNode) data.get("data");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (JsonNode item : items) {
            KlineResponse.KlineItem kline = KlineResponse.KlineItem.builder()
                    .datetime(parseDateTime(getText(item, "datetime"), formatter))
                    .open(getDecimal(item, "open"))
                    .high(getDecimal(item, "high"))
                    .low(getDecimal(item, "low"))
                    .close(getDecimal(item, "close"))
                    .volume(getLong(item, "volume"))
                    .turnover(getDecimal(item, "turnover"))
                    .changePercent(getDecimal(item, "changePercent"))
                    .build();
            klines.add(kline);
        }

        return KlineResponse.builder()
                .symbol(getText(data, "symbol"))
                .period(period)
                .klines(klines)
                .build();
    }

    private FundamentalResponse parseFundamentalResponse(JsonNode data) {
        if (data == null || !data.has("data")) {
            return FundamentalResponse.builder().build();
        }

        JsonNode item = data.get("data");
        return FundamentalResponse.builder()
                .symbol(getText(item, "symbol"))
                .marketCap(getDecimal(item, "marketCap"))
                .circulatingMarketCap(getDecimal(item, "circulatingMarketCap"))
                .totalShares(getDecimal(item, "totalShares"))
                .circulatingShares(getDecimal(item, "circulatingShares"))
                .peRatio(getDecimal(item, "peRatio"))
                .peRatioTtm(getDecimal(item, "peRatioTtm"))
                .pbRatio(getDecimal(item, "pbRatio"))
                .psRatio(getDecimal(item, "psRatio"))
                .pcRatio(getDecimal(item, "pcRatio"))
                .dividendYield(getDecimal(item, "dividendYield"))
                .bvps(getDecimal(item, "bvps"))
                .eps(getDecimal(item, "eps"))
                .epsYoy(getDecimal(item, "epsYoy"))
                .roe(getDecimal(item, "roe"))
                .roeDiluted(getDecimal(item, "roeDiluted"))
                .grossMargin(getDecimal(item, "grossMargin"))
                .netMargin(getDecimal(item, "netMargin"))
                .lastUpdated(Instant.now())
                .build();
    }

    private FinancialResponse parseFinancialResponse(JsonNode data) {
        List<FinancialResponse.FinancialIndicator> indicators = new ArrayList<>();

        if (data != null && data.has("data")) {
            ArrayNode items = (ArrayNode) data.get("data");
            for (JsonNode item : items) {
                FinancialResponse.FinancialIndicator indicator = FinancialResponse.FinancialIndicator.builder()
                        .reportDate(getText(item, "reportDate"))
                        .revenue(getDecimal(item, "revenue"))
                        .netProfit(getDecimal(item, "netProfit"))
                        .operatingProfit(getDecimal(item, "operatingProfit"))
                        .totalAssets(getDecimal(item, "totalAssets"))
                        .totalLiabilities(getDecimal(item, "totalLiabilities"))
                        .shareholdersEquity(getDecimal(item, "shareholdersEquity"))
                        .roe(getDecimal(item, "roe"))
                        .grossMargin(getDecimal(item, "grossMargin"))
                        .netMargin(getDecimal(item, "netMargin"))
                        .basicEps(getDecimal(item, "basicEps"))
                        .dilutedEps(getDecimal(item, "dilutedEps"))
                        .build();
                indicators.add(indicator);
            }
        }

        List<String> reportDates = indicators.stream()
                .map(FinancialResponse.FinancialIndicator::getReportDate)
                .toList();

        return FinancialResponse.builder()
                .symbol(null)
                .reportDates(reportDates)
                .indicators(indicators)
                .build();
    }

    private CapitalFlowResponse parseCapitalFlowResponse(JsonNode data) {
        if (data == null || !data.has("data")) {
            return CapitalFlowResponse.builder().build();
        }

        JsonNode item = data.get("data");
        return CapitalFlowResponse.builder()
                .symbol(getText(item, "symbol"))
                .mainNetInflow(getDecimal(item, "mainNetInflow"))
                .mainNetInflowRate(getDecimal(item, "mainNetInflowRate"))
                .smallNetInflow(getDecimal(item, "smallNetInflow"))
                .mediumNetInflow(getDecimal(item, "mediumNetInflow"))
                .largeNetInflow(getDecimal(item, "largeNetInflow"))
                .superNetInflow(getDecimal(item, "superNetInflow"))
                .lastUpdated(Instant.now())
                .build();
    }

    // ========== 辅助方法 ==========

    private String getText(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private BigDecimal getDecimal(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return new BigDecimal(node.get(field).asText());
    }

    private Long getLong(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        if (node.get(field).isBigInteger()) {
            return node.get(field).bigIntegerValue().longValue();
        }
        return node.get(field).asLong();
    }

    private LocalDateTime parseDateTime(String text, DateTimeFormatter formatter) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, formatter);
        } catch (Exception e) {
            return null;
        }
    }
}
