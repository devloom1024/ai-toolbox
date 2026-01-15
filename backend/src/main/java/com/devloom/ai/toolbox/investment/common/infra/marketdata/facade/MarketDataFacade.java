package com.devloom.ai.toolbox.investment.common.infra.marketdata.facade;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter.MarketDataAdapter;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result.*;
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
 * @author devloom
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
     * @param command 搜索命令
     * @return 搜索结果列表
     */
    public List<SecuritySearchResult> search(SearchCommand command) {
        log.debug("Searching for keyword: {}, market: {}", command.getKeyword(), command.getMarket());
        MarketDataAdapter adapter = router.selectForSearch();
        return adapter.search(command);
    }

    // ========== 实时行情 ==========

    /**
     * 获取实时行情。
     *
     * @param command 行情命令
     * @return 行情数据
     */
    public QuoteResult getQuote(QuoteCommand command) {
        log.debug("Getting quote for {} market: {}", command.getSymbol(), command.getMarket());
        MarketDataAdapter adapter = router.selectForKline(command.getMarket());
        return adapter.getQuote(command);
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
    public KlineResult getKline(String symbol, MarketType market, String period,
            String startDate, String endDate, int limit) {
        log.debug("Getting kline for {} market: {} period: {}", symbol, market, period);
        MarketDataAdapter adapter = router.selectForKline(market);
        KlineCommand command = KlineCommand.builder()
                .symbol(symbol)
                .market(market)
                .period(period)
                .startDate(startDate)
                .endDate(endDate)
                .limit(limit)
                .build();
        return adapter.getKline(command);
    }

    // ========== 基本面数据 ==========

    /**
     * 获取基本面数据。
     *
     * @param command 基本面命令
     * @return 基本面数据
     */
    public FundamentalResult getFundamental(FundamentalCommand command) {
        log.debug("Getting fundamental for {} market: {}", command.getSymbol(), command.getMarket());
        MarketDataAdapter adapter = router.selectForFundamental(command.getMarket());
        return adapter.getFundamental(command);
    }

    // ========== 财务指标 ==========

    /**
     * 获取财务指标。
     *
     * @param command 财务指标命令
     * @return 财务指标数据
     */
    public FinancialResult getFinancial(FinancialCommand command) {
        log.debug("Getting financial for {} market: {}", command.getSymbol(), command.getMarket());
        MarketDataAdapter adapter = router.selectForFinancial(command.getMarket());
        return adapter.getFinancial(command);
    }

    // ========== 资金流向 ==========

    /**
     * 获取资金流向。
     *
     * @param command 资金流向命令
     * @return 资金流向数据
     */
    public CapitalFlowResult getCapitalFlow(CapitalFlowCommand command) {
        log.debug("Getting capital flow for {} market: {}", command.getSymbol(), command.getMarket());
        MarketDataAdapter adapter = router.selectForCapitalFlow(command.getMarket());
        return adapter.getCapitalFlow(command);
    }
}
