package com.devloom.ai.toolbox.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Token 对返回值。
 */
@Getter
@AllArgsConstructor
public class TokenResponse {
    /** 短期访问凭证 Access Token。 */
    private final String accessToken;
    /** 长期刷新凭证 Refresh Token。 */
    private final String refreshToken;
    /** Access Token 过期秒数。 */
    private final long expiresIn;
}
