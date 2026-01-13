package com.devloom.ai.toolbox.investment.infrastructure.adapter;

import com.devloom.ai.toolbox.investment.domain.enums.KLinePeriod;
import com.devloom.ai.toolbox.investment.domain.enums.MarketType;
import com.devloom.ai.toolbox.investment.domain.model.CapitalFlowData;
import com.devloom.ai.toolbox.investment.domain.model.FundamentalData;
import com.devloom.ai.toolbox.investment.domain.model.KLineData;
import com.devloom.ai.toolbox.investment.domain.model.QuoteData;
import com.devloom.ai.toolbox.investment.domain.model.StockSearchResult;

import java.util.List;

/**
 * 市场数据适配器统一接口
 * <p>
 * 所有数据源实现都必须实现此接口,以保证数据格式的统一性
 */
public interface MarketDataAdapter {

    /**
     * 获取适配器名称
     *
     * @return 适配器名称 (如: akshare, yfinance)
     */
    String getName();

    /**
     * 获取支持的市场类型
     *
     * @return 市场类型列表
     */
    List<MarketType> getSupportedMarkets();

    /**
     * 检查是否支持指定市场
     *
     * @param market 市场类型
     * @return true-支持, false-不支持
     */
    boolean supportsMarket(MarketType market);

    /**
     * 获取实时行情数据
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 行情数据
     */
    QuoteData getQuote(String symbol, MarketType market);

    /**
     * 批量获取实时行情数据
     *
     * @param symbols 标的代码列表
     * @param market  市场类型
     * @return 行情数据列表
     */
    List<QuoteData> getBatchQuotes(List<String> symbols, MarketType market);

    /**
     * 获取K线数据
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @param period K线周期
     * @param limit  数据条数
     * @return K线数据列表
     */
    List<KLineData> getKLine(String symbol, MarketType market, KLinePeriod period, int limit);

    /**
     * 获取基本面数据
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 基本面数据
     */
    FundamentalData getFundamental(String symbol, MarketType market);

    /**
     * 获取资金流向数据
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 资金流向数据
     */
    CapitalFlowData getCapitalFlow(String symbol, MarketType market);

    /**
     * 检查适配器健康状态
     *
     * @return true-健康, false-不健康
     */
    boolean isHealthy();

    /**
     * 健康检查 (主动探测)
     *
     * @return true-健康, false-不健康
     */
    boolean healthCheck();

    /**
     * 获取适配器优先级
     * <p>
     * 数字越小优先级越高
     *
     * @return 优先级
     */
    int getPriority();

    /**
     * 搜索股票
     * <p>
     * 支持代码、名称、拼音搜索
     *
     * @param keyword 搜索关键字 (代码/名称/拼音)
     * @param market  市场类型 (可选,null表示全部市场)
     * @param limit   返回结果数量限制
     * @return 搜索结果列表 (按匹配度排序)
     */
    List<StockSearchResult> search(String keyword, MarketType market, int limit);
}
