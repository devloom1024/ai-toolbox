package com.devloom.ai.toolbox.common.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security.jwt")
public class JwtProperties {

    /**
     * 用于签名的密钥
     */
    private String secretKey = "change-me";

    /**
     * Access Token 有效期（秒）
     */
    private long accessTokenTtlSeconds = 3600;

    /**
     * 签发方
     */
    private String issuer = "ai-toolbox";

    /**
     * 时间偏移容忍（秒）
     */
    private long clockSkewSeconds = 30;
}
