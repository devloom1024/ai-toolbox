package com.devloom.ai.toolbox.investment.common.infra.marketdata.adapter;

import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command.*;
import com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.result.*;

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
     * @param command 搜索命令
     * @return 搜索结果列表
     */
    default List<SecuritySearchResult> search(SearchCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support search");
    }

    // ========== 实时行情 ==========

    /**
     * 获取实时行情。
     *
     * @param command 行情命令
     * @return 行情数据
     */
    default QuoteResult getQuote(QuoteCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support getQuote");
    }

    // ========== K 线数据 ==========

    /**
     * 获取 K 线数据。
     *
     * @param command K 线命令
     * @return K 线数据列表
     */
    default KlineResult getKline(KlineCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support getKline");
    }

    // ========== 基本面数据 ==========

    /**
     * 获取基本面数据。
     *
     * @param command 基本面命令
     * @return 基本面数据
     */
    default FundamentalResult getFundamental(FundamentalCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support getFundamental");
    }

    // ========== 财务指标 ==========

    /**
     * 获取财务指标。
     *
     * @param command 财务指标命令
     * @return 财务指标数据
     */
    default FinancialResult getFinancial(FinancialCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support getFinancial");
    }

    // ========== 资金流向 ==========

    /**
     * 获取资金流向。
     *
     * @param command 资金流向命令
     * @return 资金流向数据
     */
    default CapitalFlowResult getCapitalFlow(CapitalFlowCommand command) {
        throw new UnsupportedOperationException(getName() + " does not support getCapitalFlow");
    }
}
