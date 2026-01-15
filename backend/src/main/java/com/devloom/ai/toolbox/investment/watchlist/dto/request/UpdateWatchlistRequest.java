package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 更新自选请求参数，对应 /api/v1/investment/watchlist/{id} (PUT)。
 *
 * @author devloom
 */
@Getter
@Setter
public class UpdateWatchlistRequest {

    /** 目标分组 ID，移动标的到其他分组。 */
    private Long groupId;

    /** 备注。 */
    private String note;
}
