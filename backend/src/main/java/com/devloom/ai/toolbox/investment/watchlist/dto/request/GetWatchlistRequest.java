package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import com.devloom.ai.toolbox.common.dto.PageRequest;
import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 自选列表查询请求。
 *
 * @author claude
 */
@Getter
@SuperBuilder
public class GetWatchlistRequest extends PageRequest {

    /** 分组 ID（可选）。 */
    private Long groupId;

    /** 市场类型（可选）。 */
    private MarketType market;
}
