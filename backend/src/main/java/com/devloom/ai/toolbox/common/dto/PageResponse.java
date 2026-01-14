package com.devloom.ai.toolbox.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * 分页响应基类。
 *
 * <p>提供统一分页信息：当前页、每页大小、总记录数、总页数。</p>
 *
 * @author claude
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse {

    /** 当前页码（从 1 开始）。 */
    private Integer page;

    /** 每页大小。 */
    private Integer size;

    /** 总记录数。 */
    private Long total;

    /**
     * 计算总页数。
     */
    public int getPages() {
        if (total == null || total == 0) {
            return 0;
        }
        int pageSize = size != null && size > 0 ? size : 20;
        return (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 是否有上一页。
     */
    public boolean hasPrevious() {
        return page != null && page > 1;
    }

    /**
     * 是否有下一页。
     */
    public boolean hasNext() {
        return page != null && total != null && page < getPages();
    }

    /**
     * 是否为第一页。
     */
    public boolean isFirst() {
        return page == null || page <= 1;
    }

    /**
     * 是否为最后一页。
     */
    public boolean isLast() {
        return page == null || total == null || page >= getPages();
    }
}
