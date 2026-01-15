package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Data;

/**
 * 搜索命令。
 *
 * @author devloom
 */
@Data
public class SearchCommand {

    /** 搜索关键词（股票代码或名称）。 */
    private String keyword;

    /** 市场类型（可选）。 */
    private MarketType market;

    /** 返回结果数量限制。 */
    private int limit = 20;

    /** 分页偏移量。 */
    private int offset = 0;
}
