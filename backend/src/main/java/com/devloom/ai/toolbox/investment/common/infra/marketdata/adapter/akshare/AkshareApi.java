package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.akshare;

import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Akshare Python 网关 API 接口。
 *
 * <p>使用 @HttpExchange 注解定义 HTTP 服务客户端。</p>
 *
 * @author devloom
 */
@HttpExchange("/api/v1/akshare")
public interface AkshareApi {

    // ========== K 线数据 ==========

    @GetExchange("/kline")
    ApiResponse<KlineDataResponse> getKline(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "period", defaultValue = "daily") String period,
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate,
            @RequestParam(value = "limit", defaultValue = "500") int limit,
            @RequestParam(value = "adjust", defaultValue = "") String adjust
    );

    // ========== 基本面数据 ==========

    @GetExchange("/fundamental")
    ApiResponse<FundamentalDataResponse> getFundamental(
            @RequestParam("symbol") String symbol
    );

    // ========== 财务指标 ==========

    @GetExchange("/financial")
    ApiResponse<FinancialDataResponse> getFinancial(
            @RequestParam("symbol") String symbol,
            @RequestParam(value = "indicator", defaultValue = "report") String indicator
    );

    // ========== 资金流向 ==========

    @GetExchange("/capital-flow")
    ApiResponse<CapitalFlowDataResponse> getCapitalFlow(
            @RequestParam("symbol") String symbol
    );

    // ========== 搜索 ==========

    @GetExchange("/search")
    ApiResponse<List<SearchResult>> search(
            @RequestParam("keyword") String keyword,
            @RequestParam("market") String market,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "offset", defaultValue = "0") int offset
    );

    // ========== 内部类 - API 响应 ==========

    record ApiResponse<T>(int code, String message, T data) {
    }

    record KlineDataResponse(String symbol, String period, String adjust, List<KlineItemResponse> klines) {
    }

    record KlineItemResponse(
            String datetime,
            Double open,
            Double high,
            Double low,
            Double close,
            Long volume,
            Double turnover,
            Double changePercent,
            Double amplitude
    ) {
    }

    record FundamentalDataResponse(
            String symbol,
            String type,
            Double marketCap,
            Double circulatingMarketCap,
            Double totalShares,
            Double circulatingShares,
            Double peRatio,
            Double peRatioTtm,
            Double pbRatio,
            Double psRatio,
            Double pcRatio,
            Double dividendYield,
            Double bvps,
            Double eps,
            Double epsYoy,
            Double roe,
            Double roeDiluted,
            Double grossMargin,
            Double netMargin,
            String industry,
            String listedDate,
            String lastUpdated
    ) {
    }

    record FinancialDataResponse(
            String symbol,
            String type,
            String indicator,
            List<String> reportDates,
            List<FinancialIndicatorResponse> indicators
    ) {
    }

    record FinancialIndicatorResponse(
            String reportDate,
            Double revenue,
            Double netProfit,
            Double operatingProfit,
            Double totalAssets,
            Double totalLiabilities,
            Double shareholdersEquity,
            Double roe,
            Double roeDiluted,
            Double grossMargin,
            Double netMargin,
            Double basicEps,
            Double dilutedEps,
            Double bvps
    ) {
    }

    record CapitalFlowDataResponse(
            String symbol,
            String type,
            Double mainNetInflow,
            Double mainNetInflowRate,
            Double superNetInflow,
            Double superNetInflowRate,
            Double largeNetInflow,
            Double largeNetInflowRate,
            Double mediumNetInflow,
            Double mediumNetInflowRate,
            Double smallNetInflow,
            Double smallNetInflowRate,
            String error,
            String lastUpdated
    ) {
    }

    record SearchResult(String symbol, String name, String market, String type) {
    }
}
