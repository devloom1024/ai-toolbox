package com.devloom.ai.toolbox.auth.dto.response;

import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 用户资料响应。
 */
@Getter
@Builder
public class ProfileResponse {

    /** 用户 ID。 */
    private final Long userId;
    /** 显示昵称。 */
    private final String nickname;
    /** 头像 URL。 */
    private final String avatar;
    /** 账号状态。 */
    private final UserStatus status;
    /** 绑定方式列表。 */
    private final List<AuthBindingResponse> bindings;

    /**
     * 单个绑定项。
     */
    @Getter
    @Builder
    public static class AuthBindingResponse {
        /** 绑定类型，例如 EMAIL、LINUX_DO。 */
        private final IdentityType type;
        /** 标识（邮箱或第三方账号）。 */
        private final String identifier;
        /** 是否已验证。 */
        private final boolean verified;
    }
}
