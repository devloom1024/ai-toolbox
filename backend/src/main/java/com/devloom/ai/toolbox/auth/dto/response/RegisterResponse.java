package com.devloom.ai.toolbox.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResponse {
    private final Long userId;
    private final TokenResponse token;
}
