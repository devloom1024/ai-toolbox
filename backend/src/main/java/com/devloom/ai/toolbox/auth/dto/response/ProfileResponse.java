package com.devloom.ai.toolbox.auth.dto.response;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {

    private final Long userId;
    private final String nickname;
    private final String avatar;
    private final UserStatus status;
    private final List<AuthBindingResponse> bindings;

    @Getter
    @Builder
    public static class AuthBindingResponse {
        private final IdentityType type;
        private final String identifier;
        private final boolean verified;
    }
}
