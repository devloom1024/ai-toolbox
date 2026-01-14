package com.devloom.ai.toolbox.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 分页查询请求基类。
 *
 * <p>提供统一分页参数：页码和每页大小。</p>
 *
 * @author claude
 */
@Getter
@SuperBuilder
public class PageRequest {

    /** 页码（从 1 开始，默认 1）。 */
    private Integer page;

    /** 每页大小（默认 20）。 */
    private Integer size;

    /**
     * 获取分页页码（从 0 开始，适用于 Spring Data JPA）。
     */
    public int getPageZeroBased() {
        int pageNum = page != null && page > 0 ? page : 1;
        return pageNum - 1;
    }

    /**
     * 获取分页大小。
     */
    public int getPageSize() {
        return size != null && size > 0 ? size : 20;
    }

    /**
     * 获取页码（从 1 开始，无效值返回 1）。
     */
    public int getPageOneBased() {
        return page != null && page > 0 ? page : 1;
    }
}
