package com.devloom.ai.toolbox.investment.watchlist.service;

import com.devloom.ai.toolbox.common.dto.PageRequest;
import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 自选列表查询参数。
 *
 * @author devloom
 */
@Getter
@SuperBuilder
public class WatchlistQuery extends PageRequest {

    /** 分组 ID（可选）。 */
    private Long groupId;

    /** 市场类型（可选）。 */
    private MarketType market;
}
