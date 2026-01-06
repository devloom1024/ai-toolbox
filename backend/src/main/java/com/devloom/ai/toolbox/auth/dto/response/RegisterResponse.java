package com.devloom.ai.toolbox.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 注册成功返回结果。
 */
@Getter
@Builder
public class RegisterResponse {
    /** 新用户主键 ID。 */
    private final Long userId;
    /** 初次登录的 Token 对。 */
    private final TokenResponse token;
}
