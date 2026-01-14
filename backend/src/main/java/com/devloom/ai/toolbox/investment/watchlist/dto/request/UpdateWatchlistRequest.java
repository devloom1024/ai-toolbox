package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新自选请求参数，对应 /api/v1/investment/watchlist/{id} (PUT)。
 *
 * @author claude
 */
@Getter
@Setter
public class UpdateWatchlistRequest {

    /** 目标分组 ID，移动标的到其他分组。 */
    private Long groupId;

    /** 备注。 */
    private String note;
}
