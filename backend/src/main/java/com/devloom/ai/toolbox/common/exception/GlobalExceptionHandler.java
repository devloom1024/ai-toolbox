package com.devloom.ai.toolbox.common.exception;

import com.devloom.ai.toolbox.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BizException.class)
    public ResponseEntity<ApiResponse<Void>> handleBizException(BizException ex, Locale locale) {
        HttpStatus status = mapStatus(ex.getErrorCode());
        String message = resolveMessage(ex.getErrorCode(), locale, ex.getMessageArgs());
        return ResponseEntity.status(status).body(ApiResponse.error(ex.getErrorCode().getCode(), message));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex, Locale locale) {
        String message = ex.getMessage();
        if (ex instanceof MethodArgumentNotValidException manv) {
            message = manv.getBindingResult().getAllErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse(message);
        } else if (ex instanceof BindException bindException) {
            message = bindException.getBindingResult().getAllErrors().stream()
                    .findFirst()
                    .map(error -> error.getDefaultMessage())
                    .orElse(message);
        }
        String localized = messageSource.getMessage(BizErrorCode.INVALID_PARAMETER.getMessageKey(), null, message, locale);
        return ResponseEntity.badRequest().body(ApiResponse.error(BizErrorCode.INVALID_PARAMETER.getCode(), localized));
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(Locale locale) {
        String message = resolveMessage(BizErrorCode.RESOURCE_NOT_FOUND, locale);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(BizErrorCode.RESOURCE_NOT_FOUND.getCode(), message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(Locale locale) {
        String message = resolveMessage(BizErrorCode.ACCESS_DENIED, locale);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(BizErrorCode.ACCESS_DENIED.getCode(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOthers(Exception ex, Locale locale, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);
        String message = resolveMessage(BizErrorCode.INTERNAL_ERROR, locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(BizErrorCode.INTERNAL_ERROR.getCode(), message));
    }

    private HttpStatus mapStatus(BizErrorCode code) {
        return switch (code) {
            case UNAUTHORIZED, REFRESH_TOKEN_INVALID, PASSWORD_MISMATCH -> HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case RATE_LIMIT -> HttpStatus.TOO_MANY_REQUESTS;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_REQUEST, EMAIL_EXISTS, OTP_INVALID, OTP_EXPIRED, INVALID_PARAMETER ->
                    HttpStatus.BAD_REQUEST;
            case FEATURE_NOT_IMPLEMENTED, LINUXDO_NOT_CONFIGURED -> HttpStatus.NOT_IMPLEMENTED;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    private String resolveMessage(BizErrorCode code, Locale locale, Object... args) {
        return messageSource.getMessage(code.getMessageKey(), args, code.getDefaultMessage(), locale);
    }
}
