package com.devloom.ai.toolbox.auth.service.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private VerificationCodeProperties verificationCode = new VerificationCodeProperties();
    private TokenProperties token = new TokenProperties();
    private LinuxDoProperties linuxdo = new LinuxDoProperties();

    @Getter
    @Setter
    public static class VerificationCodeProperties {
        /**
         * 验证码有效期（分钟）
         */
        private long expireMinutes = 10;
        /**
         * 同一个标识符发码的最小间隔（秒）
         */
        private long requestIntervalSeconds = 60;
    }

    @Getter
    @Setter
    public static class TokenProperties {
        /**
         * Refresh Token 默认有效期（天）
         */
        private long refreshTokenTtlDays = 30;

        /**
         * 默认的设备标识（用户未上报时）
         */
        private String defaultDevice = "UNKNOWN";
    }

    @Getter
    @Setter
    public static class LinuxDoProperties {
        private String clientId = "";
        private String clientSecret = "";
        private String authorizeUrl = "";
        private String tokenUrl = "";
        private String profileUrl = "";
        private String redirectBaseUrl = "";
        private long stateTtlMinutes = 5;
    }
}
