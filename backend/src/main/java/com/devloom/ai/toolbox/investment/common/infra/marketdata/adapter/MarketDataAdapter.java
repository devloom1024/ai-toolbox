package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.*;

import java.util.List;

/**
 * 市场数据适配器接口。
 *
 * <p>定义统一的数据访问接口，支持按功能选择数据源。</p>
 *
 * @author claude
 */
public interface MarketDataAdapter {

    /**
     * 获取适配器名称。
     */
    String getName();

    /**
     * 获取优先级（数字越小优先级越高）。
     */
    int getPriority();

    /**
     * 检查适配器是否可用。
     */
    boolean isAvailable();

    // ========== 搜索功能 ==========

    /**
     * 搜索标的。
     *
     * @param request 搜索请求
     * @return 搜索结果列表
     */
    List<SecuritySearchResult> search(SearchRequest request);

    // ========== 实时行情 ==========

    /**
     * 获取实时行情。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 行情数据
     */
    QuoteResponse getQuote(String symbol, String market);

    // ========== K 线数据 ==========

    /**
     * 获取 K 线数据。
     *
     * @param request K 线请求
     * @return K 线数据列表
     */
    KlineResponse getKline(KlineRequest request);

    // ========== 基本面数据 ==========

    /**
     * 获取基本面数据。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 基本面数据
     */
    FundamentalResponse getFundamental(String symbol, String market);

    // ========== 财务指标 ==========

    /**
     * 获取财务指标。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 财务指标数据
     */
    FinancialResponse getFinancial(String symbol, String market);

    // ========== 资金流向 ==========

    /**
     * 获取资金流向。
     *
     * @param symbol 标的代码
     * @param market 市场类型
     * @return 资金流向数据
     */
    CapitalFlowResponse getCapitalFlow(String symbol, String market);
}
