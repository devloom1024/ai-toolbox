package com.devloom.ai.toolbox.investment.watchlist.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 检查自选状态响应。
 *
 * @author devloom
 */
@Getter
@Builder
public class WatchlistCheckResponse {

    /** 是否已在自选列表中。 */
    private Boolean inWatchlist;

    /** 自选记录 ID（如果存在）。 */
    private Long watchlistId;

    /** 所在分组 ID（如果存在）。 */
    private Long groupId;
}
