package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.request.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.response.*;

import java.util.List;

/**
 * 市场数据适配器接口。
 *
 * <p>定义统一的数据访问接口，支持按功能选择数据源。</p>
 *
 * @author devloom
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
    default List<SecuritySearchResponse> search(SearchRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support search");
    }

    // ========== 实时行情 ==========

    /**
     * 获取实时行情。
     *
     * @param request 行情请求
     * @return 行情数据
     */
    default QuoteResponse getQuote(QuoteRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support getQuote");
    }

    // ========== K 线数据 ==========

    /**
     * 获取 K 线数据。
     *
     * @param request K 线请求
     * @return K 线数据列表
     */
    default KlineResponse getKline(KlineRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support getKline");
    }

    // ========== 基本面数据 ==========

    /**
     * 获取基本面数据。
     *
     * @param request 基本面请求
     * @return 基本面数据
     */
    default FundamentalResponse getFundamental(FundamentalRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support getFundamental");
    }

    // ========== 财务指标 ==========

    /**
     * 获取财务指标。
     *
     * @param request 财务指标请求
     * @return 财务指标数据
     */
    default FinancialResponse getFinancial(FinancialRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support getFinancial");
    }

    // ========== 资金流向 ==========

    /**
     * 获取资金流向。
     *
     * @param request 资金流向请求
     * @return 资金流向数据
     */
    default CapitalFlowResponse getCapitalFlow(CapitalFlowRequest request) {
        throw new UnsupportedOperationException(getName() + " does not support getCapitalFlow");
    }
}
