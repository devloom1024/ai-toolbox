package com.devloom.ai.toolbox.auth.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 登录审计状态。
 */
@Getter
@RequiredArgsConstructor
public enum LoginStatus {
    /** 登录成功。 */
    SUCCESS(1),
    /** 登录失败。 */
    FAILURE(2);

    private final int value;
}
