package com.devloom.ai.toolbox.investment.watchlist.dto.response;

import com.devloom.ai.toolbox.common.dto.PageResponse;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 自选列表响应。
 *
 * @author claude
 */
@Getter
@SuperBuilder
public class WatchlistDataResponse extends PageResponse {

    /** 自选列表。 */
    private List<WatchlistItemResponse> items;
}
