package com.devloom.ai.toolbox.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 刷新 Token 请求参数。
 */
@Getter
@Setter
public class RefreshTokenRequest {

    /** 长期凭证 Refresh Token。 */
    @NotBlank
    private String refreshToken;
}
