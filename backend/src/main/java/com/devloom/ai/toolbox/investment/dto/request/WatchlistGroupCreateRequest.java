package com.devloom.ai.toolbox.investment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建自选分组请求
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistGroupCreateRequest {

    /**
     * 分组名称，最大长度 64 字符
     */
    @NotBlank(message = "{validation.group.name.required}")
    private String name;

    /**
     * 排序序号，不传则默认 0
     */
    private Short sortOrder;
}
