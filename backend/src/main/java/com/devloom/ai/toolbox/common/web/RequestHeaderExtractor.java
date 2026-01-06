package com.devloom.ai.toolbox.common.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

@UtilityClass
public class RequestHeaderExtractor {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";

    public static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public static String resolveUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(HEADER_USER_AGENT);
        return StringUtils.hasText(userAgent) ? userAgent : "";
    }
}
