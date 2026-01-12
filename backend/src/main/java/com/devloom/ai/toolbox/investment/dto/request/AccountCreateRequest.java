package com.devloom.ai.toolbox.investment.dto.request;

import com.devloom.ai.toolbox.investment.domain.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建投资账号请求
 *
 * @author huangkl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreateRequest {

    /**
     * 账号类型
     */
    @NotNull(message = "{validation.account.type.required}")
    private AccountType accountType;

    /**
     * 账号名称
     */
    @NotBlank(message = "{validation.account.name.required}")
    private String accountName;

    /**
     * 账号图标 URL
     */
    private String accountIcon;

    /**
     * 排序序号
     */
    private Short sortOrder;
}
