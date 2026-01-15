package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.domain.enums.SecurityType;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.router.MarketDataFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Akshare 数据源适配器。
 *
 * <p>通过 Python Gateway 调用 akshare 库获取市场数据。</p>
 *
 * @author devloom
 */
@Slf4j
@Component
public class AkshareAdapter implements MarketDataAdapter {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AkshareApi akshareApi;

    @Autowired
    public AkshareAdapter(AkshareApi akshareApi) {
        this.akshareApi = akshareApi;
    }

    @Override
    public String getName() {
        return "akshare";
    }

    @Override
    public Set<MarketType> getSupportedMarkets() {
        return Set.of(MarketType.A_SHARE);
    }

    @Override
    public Set<MarketDataFeature> getSupportedFeatures() {
        return Set.of(
                MarketDataFeature.SEARCH,
                MarketDataFeature.KLINE,
                MarketDataFeature.FUNDAMENTAL,
                MarketDataFeature.FINANCIAL,
                MarketDataFeature.CAPITAL_FLOW
        );
    }

    @Override
    public boolean isAvailable() {
        try {
            akshareApi.getKline("600000", "daily", "", "", 1, "");
            return true;
        } catch (Exception e) {
            log.warn("Akshare service health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ========== 搜索功能 ==========

    @Override
    public List<SecuritySearchResult> search(SearchCommand command) {
        String market = command.getMarket() != null ? command.getMarket().getCode() : null;
        AkshareApi.ApiResponse<List<AkshareApi.SearchResult>> response = akshareApi.search(
                command.getKeyword(),
                market,
                command.getLimit(),
                command.getOffset()
        );

        if (response == null || response.data() == null) {
            return Collections.emptyList();
        }

        List<SecuritySearchResult> results = new ArrayList<>();
        for (AkshareApi.SearchResult r : response.data()) {
            SecuritySearchResult result = new SecuritySearchResult();
            result.setSymbol(r.symbol());
            result.setName(r.name());
            result.setMarket(convertMarketType(r.market()));
            result.setType(convertSecurityType(r.type()));
            results.add(result);
        }
        return results;
    }

    // ========== K 线数据 ==========

    @Override
    public KlineResult getKline(KlineCommand command) {
        AkshareApi.ApiResponse<AkshareApi.KlineDataResponse> response = akshareApi.getKline(
                command.getSymbol(),
                command.getPeriod() != null ? command.getPeriod() : "daily",
                command.getStartDate() != null ? command.getStartDate() : "",
                command.getEndDate() != null ? command.getEndDate() : "",
                command.getLimit() > 0 ? command.getLimit() : 500,
                ""
        );

        KlineResult result = new KlineResult();
        if (response == null || response.data() == null) {
            result.setSymbol(command.getSymbol());
            result.setPeriod(command.getPeriod());
            result.setKlines(Collections.emptyList());
            return result;
        }

        AkshareApi.KlineDataResponse data = response.data();
        List<KlineResult.KlineItem> klines = new ArrayList<>();
        if (data.klines() != null) {
            for (AkshareApi.KlineItemResponse item : data.klines()) {
                KlineResult.KlineItem klineItem = new KlineResult.KlineItem();
                klineItem.setDatetime(parseLocalDateTime(item.datetime()));
                klineItem.setOpen(toBigDecimal(item.open()));
                klineItem.setHigh(toBigDecimal(item.high()));
                klineItem.setLow(toBigDecimal(item.low()));
                klineItem.setClose(toBigDecimal(item.close()));
                klineItem.setVolume(item.volume());
                klineItem.setTurnover(toBigDecimal(item.turnover()));
                klineItem.setChangePercent(toBigDecimal(item.changePercent()));
                klines.add(klineItem);
            }
        }

        result.setSymbol(data.symbol());
        result.setPeriod(data.period());
        result.setKlines(klines);
        return result;
    }

    // ========== 基本面数据 ==========

    @Override
    public FundamentalResult getFundamental(FundamentalCommand command) {
        AkshareApi.ApiResponse<AkshareApi.FundamentalDataResponse> response = akshareApi.getFundamental(command.getSymbol());

        FundamentalResult result = new FundamentalResult();
        if (response == null || response.data() == null) {
            result.setSymbol(command.getSymbol());
            result.setLastUpdated(Instant.now());
            return result;
        }

        AkshareApi.FundamentalDataResponse data = response.data();
        result.setSymbol(data.symbol());
        result.setMarketCap(toBigDecimal(data.marketCap()));
        result.setCirculatingMarketCap(toBigDecimal(data.circulatingMarketCap()));
        result.setTotalShares(toBigDecimal(data.totalShares()));
        result.setCirculatingShares(toBigDecimal(data.circulatingShares()));
        result.setPeRatio(toBigDecimal(data.peRatio()));
        result.setPeRatioTtm(toBigDecimal(data.peRatioTtm()));
        result.setPbRatio(toBigDecimal(data.pbRatio()));
        result.setPsRatio(toBigDecimal(data.psRatio()));
        result.setPcRatio(toBigDecimal(data.pcRatio()));
        result.setDividendYield(toBigDecimal(data.dividendYield()));
        result.setBvps(toBigDecimal(data.bvps()));
        result.setEps(toBigDecimal(data.eps()));
        result.setEpsYoy(toBigDecimal(data.epsYoy()));
        result.setRoe(toBigDecimal(data.roe()));
        result.setRoeDiluted(toBigDecimal(data.roeDiluted()));
        result.setGrossMargin(toBigDecimal(data.grossMargin()));
        result.setNetMargin(toBigDecimal(data.netMargin()));
        result.setLastUpdated(parseInstant(data.lastUpdated()));
        return result;
    }

    // ========== 财务指标 ==========

    @Override
    public FinancialResult getFinancial(FinancialCommand command) {
        AkshareApi.ApiResponse<AkshareApi.FinancialDataResponse> response = akshareApi.getFinancial(command.getSymbol(), "report");

        FinancialResult result = new FinancialResult();
        if (response == null || response.data() == null) {
            result.setSymbol(command.getSymbol());
            result.setReportDates(Collections.emptyList());
            result.setIndicators(Collections.emptyList());
            return result;
        }

        AkshareApi.FinancialDataResponse data = response.data();
        List<FinancialResult.FinancialIndicator> indicators = new ArrayList<>();
        if (data.indicators() != null) {
            for (AkshareApi.FinancialIndicatorResponse item : data.indicators()) {
                FinancialResult.FinancialIndicator indicator = new FinancialResult.FinancialIndicator();
                indicator.setReportDate(item.reportDate());
                indicator.setRevenue(toBigDecimal(item.revenue()));
                indicator.setNetProfit(toBigDecimal(item.netProfit()));
                indicator.setOperatingProfit(toBigDecimal(item.operatingProfit()));
                indicator.setTotalAssets(toBigDecimal(item.totalAssets()));
                indicator.setTotalLiabilities(toBigDecimal(item.totalLiabilities()));
                indicator.setShareholdersEquity(toBigDecimal(item.shareholdersEquity()));
                indicator.setRoe(toBigDecimal(item.roe()));
                indicator.setGrossMargin(toBigDecimal(item.grossMargin()));
                indicator.setNetMargin(toBigDecimal(item.netMargin()));
                indicator.setBasicEps(toBigDecimal(item.basicEps()));
                indicator.setDilutedEps(toBigDecimal(item.dilutedEps()));
                indicators.add(indicator);
            }
        }

        result.setSymbol(data.symbol());
        result.setReportDates(data.reportDates() != null ? data.reportDates() : Collections.emptyList());
        result.setIndicators(indicators);
        return result;
    }

    // ========== 资金流向 ==========

    @Override
    public CapitalFlowResult getCapitalFlow(CapitalFlowCommand command) {
        AkshareApi.ApiResponse<AkshareApi.CapitalFlowDataResponse> response = akshareApi.getCapitalFlow(command.getSymbol());

        CapitalFlowResult result = new CapitalFlowResult();
        if (response == null || response.data() == null) {
            result.setSymbol(command.getSymbol());
            result.setLastUpdated(Instant.now());
            return result;
        }

        AkshareApi.CapitalFlowDataResponse data = response.data();
        result.setSymbol(data.symbol());
        result.setMainNetInflow(toBigDecimal(data.mainNetInflow()));
        result.setMainNetInflowRate(toBigDecimal(data.mainNetInflowRate()));
        result.setSmallNetInflow(toBigDecimal(data.smallNetInflow()));
        result.setMediumNetInflow(toBigDecimal(data.mediumNetInflow()));
        result.setLargeNetInflow(toBigDecimal(data.largeNetInflow()));
        result.setSuperNetInflow(toBigDecimal(data.superNetInflow()));
        result.setLastUpdated(parseInstant(data.lastUpdated()));
        return result;
    }

    // ========== 辅助方法 ==========

    private MarketType convertMarketType(String market) {
        if (market == null) return null;
        return switch (market) {
            case "A_SHARE" -> MarketType.A_SHARE;
            case "HK" -> MarketType.HK;
            case "US" -> MarketType.US;
            case "ETF" -> MarketType.ETF;
            case "FUND" -> MarketType.FUND;
            default -> null;
        };
    }

    private SecurityType convertSecurityType(String type) {
        if (type == null) return null;
        return switch (type) {
            case "STOCK" -> SecurityType.STOCK;
            case "ETF" -> SecurityType.ETF;
            case "FUND" -> SecurityType.FUND;
            default -> null;
        };
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDateTime.parse(value, DATETIME_FORMATTER);
        } catch (Exception e) {
            log.debug("Failed to parse datetime: {}", value);
            return null;
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty()) return Instant.now();
        try {
            return Instant.parse(value);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
