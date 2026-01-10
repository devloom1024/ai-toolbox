package com.devloom.ai.toolbox.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BizErrorCode {

    SUCCESS(0, "general.ok", "ok"),
    INVALID_PARAMETER(1000, "error.invalid-parameter", "Invalid parameter"),
    UNAUTHORIZED(1001, "error.unauthorized", "Unauthorized"),
    ACCESS_DENIED(1002, "error.access-denied", "Access denied"),
    RESOURCE_NOT_FOUND(1003, "error.resource-not-found", "Resource not found"),
    DUPLICATE_REQUEST(1004, "error.duplicate-request", "Duplicate request"),
    RATE_LIMIT(1005, "error.rate-limit", "Too many requests"),
    OTP_INVALID(1010, "error.verification-code.invalid", "Invalid verification code"),
    OTP_EXPIRED(1011, "error.verification-code.expired", "Verification code expired"),
    EMAIL_EXISTS(2001, "auth.email.exists", "Email already registered"),
    ACCOUNT_LOCKED(2002, "auth.account.locked", "Account is locked"),
    PASSWORD_MISMATCH(2003, "auth.password.mismatch", "Password mismatch"),
    REFRESH_TOKEN_INVALID(2004, "auth.refresh-token.invalid", "Refresh token invalid"),
    LINUXDO_NOT_CONFIGURED(2005, "auth.linuxdo.not-configured", "LinuxDo integration is not configured"),
    LINUXDO_ALREADY_BIND(2006, "auth.linuxdo.already-bind", "LinuxDo account is already bound to another user"),
    FEATURE_NOT_IMPLEMENTED(9000, "error.not-implemented", "Feature not implemented"),
    INTERNAL_ERROR(9999, "error.internal", "Internal server error");

    private final int code;
    private final String messageKey;
    private final String defaultMessage;
}
