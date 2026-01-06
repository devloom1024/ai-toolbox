package com.devloom.ai.toolbox.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * LinuxDo 授权响应，对应 authorize 接口。
 */
@Getter
@Builder
public class LinuxDoAuthorizeResponse {
    /** 防 CSRF 的 state 值。 */
    private final String state;
    /** 前端跳转的授权链接。 */
    private final String authorizeUrl;
}
