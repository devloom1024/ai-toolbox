package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Data;

/**
 * 资金流向命令。
 *
 * @author devloom
 */
@Data
public class CapitalFlowCommand {

    /** 标的代码。 */
    private String symbol;

    /** 市场类型。 */
    private MarketType market;
}
