package com.devloom.ai.toolbox.investment.search.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 搜索结果响应。
 *
 * @author devloom
 */
@Getter
@Builder
public class SearchResultResponse {

    /** 搜索结果列表。 */
    private List<SecuritySearchItem> securities;

    /** 搜索结果总数。 */
    private Long total;

    /** 当前限制数量。 */
    private Integer limit;

    /** 当前偏移量。 */
    private Integer offset;
}
