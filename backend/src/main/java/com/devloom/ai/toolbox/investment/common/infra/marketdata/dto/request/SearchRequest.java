package com.devloom.ai.toolbox.investment.common.infra.marketdata.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 搜索请求。
 *
 * @author devloom
 */
@Getter
@Builder
public class SearchRequest {

    /** 搜索关键词（股票代码或名称）。 */
    private String keyword;

    /** 市场类型（可选）。 */
    private String market;

    /** 返回结果数量限制。 */
    private int limit = 20;

    /** 分页偏移量。 */
    private int offset = 0;
}
