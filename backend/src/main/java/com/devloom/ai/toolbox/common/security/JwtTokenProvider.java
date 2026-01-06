package com.devloom.ai.toolbox.common.security;

import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;
    private final Clock clock;

    private Key signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String nickname) {
        Instant now = clock.instant();
        Instant expiry = now.plusSeconds(properties.getAccessTokenTtlSeconds());
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuer(properties.getIssuer())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("nickname", nickname)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public JwtPayload parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .setAllowedClockSkewSeconds(properties.getClockSkewSeconds())
                    .build()
                    .parseClaimsJws(token);
            Claims body = jws.getBody();
            Long userId = Long.valueOf(body.getSubject());
            Instant issuedAt = body.getIssuedAt().toInstant();
            Instant expiresAt = body.getExpiration().toInstant();
            String nickname = body.get("nickname", String.class);
            return new JwtPayload(userId, nickname, issuedAt, expiresAt);
        } catch (Exception ex) {
            throw new BizException(BizErrorCode.UNAUTHORIZED);
        }
    }

    public long getAccessTokenTtlSeconds() {
        return properties.getAccessTokenTtlSeconds();
    }

    public record JwtPayload(Long userId, String nickname, Instant issuedAt, Instant expiresAt) {
    }
}
