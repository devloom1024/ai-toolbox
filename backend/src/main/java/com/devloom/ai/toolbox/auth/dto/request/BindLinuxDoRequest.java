package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * LinuxDo 绑定请求参数。
 */
@Getter
@Setter
public class BindLinuxDoRequest {

    /** LinuxDo OAuth 返回的授权码。 */
    @NotBlank(message = "{validation.oauth.code.required}")
    private String code;

    /** 与 authorize 响应对应的 state。 */
    @NotBlank(message = "{validation.oauth.state.required}")
    private String state;
}
