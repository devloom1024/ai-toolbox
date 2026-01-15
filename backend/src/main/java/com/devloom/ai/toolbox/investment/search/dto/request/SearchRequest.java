package com.devloom.ai.toolbox.investment.search.dto.request;

import com.devloom.ai.toolbox.investment.common.domain.enums.MarketType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 搜索请求参数，对应 /api/v1/investment/search。
 *
 * @author devloom
 */
@Getter
@Setter
public class SearchRequest {

    /** 搜索关键词（股票代码或名称）。 */
    @NotBlank(message = "{validation.keyword.required}")
    private String keyword;

    /** 市场类型。 */
    private MarketType market = MarketType.ALL;

    /** 返回结果数量限制。 */
    @Min(value = 1, message = "{validation.limit.min}")
    @Max(value = 100, message = "{validation.limit.max}")
    private Integer limit = 20;

    /** 分页偏移量。 */
    @Min(value = 0, message = "{validation.offset.min}")
    private Integer offset = 0;
}
