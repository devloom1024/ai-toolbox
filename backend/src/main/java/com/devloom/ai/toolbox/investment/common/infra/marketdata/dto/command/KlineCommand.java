package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * K 线命令。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class KlineCommand {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private MarketType market;

    /** K 线周期：D/W/M/5/15/30/60。 */
    private String period;

    /** 开始日期 (YYYYMMDD)。 */
    private String startDate;

    /** 结束日期 (YYYYMMDD)。 */
    private String endDate;

    /** 数据条数限制。 */
    @Builder.Default
    private int limit = 500;
}
