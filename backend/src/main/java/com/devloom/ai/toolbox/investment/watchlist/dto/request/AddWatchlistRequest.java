package com.devloom.ai.toolbox.investment.watchlist.dto.request;

import com.devloom.ai.toolbox.investment.watchlist.domain.enums.MarketType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 添加自选请求参数，对应 /api/v1/investment/watchlist (POST)。
 *
 * @author claude
 */
@Getter
@Setter
public class AddWatchlistRequest {

    /** 标的代码（纯数字或字母）。 */
    @NotBlank(message = "{validation.symbol.required}")
    private String symbol;

    /** 市场类型。 */
    @NotBlank(message = "{validation.market.required}")
    private MarketType market;

    /** 分组 ID，不传则添加到默认分组。 */
    private Long groupId;

    /** 备注（可选）。 */
    private String note;
}
