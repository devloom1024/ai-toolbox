package com.devloom.ai.toolbox.auth.domain.enums;

/**
 * 用户状态。
 */
public enum UserStatus {
    /** 正常可用。 */
    ACTIVE,
    /** 被锁定，禁止登录。 */
    LOCKED,
    /** 已注销/删除。 */
    DELETED
}
