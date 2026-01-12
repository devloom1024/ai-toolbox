package com.devloom.ai.toolbox.common.security;

import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrentUserHolder {

    private static final ThreadLocal<CurrentUser> USER = new ThreadLocal<>();

    public static void setCurrentUser(@Nullable CurrentUser currentUser) {
        if (currentUser == null) {
            USER.remove();
        } else {
            USER.set(currentUser);
        }
    }

    public static CurrentUser getCurrentUser() {
        CurrentUser user = USER.get();
        if (user == null) {
            // 尝试从 SecurityContextHolder 获取
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof CurrentUser) {
                return (CurrentUser) authentication.getPrincipal();
            }
            throw new BizException(BizErrorCode.UNAUTHORIZED);
        }
        return user;
    }

    public static Long getUserId() {
        return getCurrentUser().getUserId();
    }

    public static String getNickname() {
        return getCurrentUser().getNickname();
    }

    public static void clear() {
        USER.remove();
    }
}
