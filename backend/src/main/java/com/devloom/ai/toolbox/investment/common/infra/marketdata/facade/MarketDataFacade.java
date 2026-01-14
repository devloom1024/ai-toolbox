package com.devloom.ai.toolbox.investment.common.infra.marketdata.facade;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.router.DataSourceRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 金融数据访问门面。
 *
 * <p>统一入口访问各种金融数据，隐藏底层数据源细节。</p>
 *
 * @author claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataFacade {

    private final DataSourceRouter router;

    // ========== 搜索功能 ==========

    /**
     * 搜索标的。
     *
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    public List<SecuritySearchResult> search(SearchRequest request) {
        log.debug("Searching for keyword: {}, market: {}", request.getKeyword(), request.getMarket());
        MarketDataAdapter adapter = router.selectForSearch();
        return adapter.search(request);
    }

    // ========== 实时行情 ==========

    /**
     * 获取实时行情。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 行情数据
     */
    public QuoteResponse getQuote(String symbol, String market) {
        log.debug("Getting quote for {} market: {}", symbol, market);
        MarketDataAdapter adapter = router.selectForKline(market);
        return adapter.getQuote(symbol, market);
    }

    // ========== K 线数据 ==========

    /**
     * 获取 K 线数据。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @param period K 线周期 (D/W/M/5/15/30/60)
     * @param startDate 开始日期 (YYYYMMDD)
     * @param endDate 结束日期 (YYYYMMDD)
     * @param limit 数据条数限制
     * @return K 线数据列表
     */
    public KlineResponse getKline(String symbol, String market, String period,
            String startDate, String endDate, int limit) {
        log.debug("Getting kline for {} market: {} period: {}", symbol, market, period);
        MarketDataAdapter adapter = router.selectForKline(market);
        KlineRequest request = KlineRequest.builder()
                .symbol(symbol)
                .market(market)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();
        return adapter.getKline(request);
    }

    // ========== 基本面数据 ==========

    /**
     * 获取基本面数据。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 基本面数据
     */
    public FundamentalResponse getFundamental(String symbol, String market) {
        log.debug("Getting fundamental for {} market: {}", symbol, market);
        MarketDataAdapter adapter = router.selectForFundamental(market);
        return adapter.getFundamental(symbol, market);
    }

    // ========== 财务指标 ==========

    /**
     * 获取财务指标。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 财务指标数据
     */
    public FinancialResponse getFinancial(String symbol, String market) {
        log.debug("Getting financial for {} market: {}", symbol, market);
        MarketDataAdapter adapter = router.selectForFinancial(market);
        return adapter.getFinancial(symbol, market);
    }

    // ========== 资金流向 ==========

    /**
     * 获取资金流向。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 资金流向数据
     */
    public CapitalFlowResponse getCapitalFlow(String symbol, String market) {
        log.debug("Getting capital flow for {} market: {}", symbol, market);
        MarketDataAdapter adapter = router.selectForCapitalFlow(market);
        return adapter.getCapitalFlow(symbol, market);
    }
}
