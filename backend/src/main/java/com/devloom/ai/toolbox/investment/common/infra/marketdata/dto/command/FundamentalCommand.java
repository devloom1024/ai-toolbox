package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 基本面命令。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class FundamentalCommand {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private MarketType market;
}
