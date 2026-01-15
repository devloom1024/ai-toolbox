package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.command;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 搜索命令。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class SearchCommand {

    /** 搜索关键词（股票代码或名称）。 */
    private String keyword;

    /** 市场类型（可选）。 */
    private MarketType market;

    /** 返回结果数量限制。 */
    @Builder.Default
    private int limit = 20;

    /** 分页偏移量。 */
    @Builder.Default
    private int offset = 0;
}
