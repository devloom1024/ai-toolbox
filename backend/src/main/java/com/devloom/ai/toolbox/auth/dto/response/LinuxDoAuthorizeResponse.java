package com.devloom.ai.toolbox.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinuxDoAuthorizeResponse {
    private final String state;
    private final String authorizeUrl;
}
