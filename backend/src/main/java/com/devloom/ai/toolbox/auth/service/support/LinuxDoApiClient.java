package com.devloom.ai.toolbox.auth.service.support;

import com.devloom.ai.toolbox.auth.service.support.AuthProperties.LinuxDoProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * LinuxDo API 客户端
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LinuxDoApiClient {

    private final AuthProperties authProperties;
    private final RestTemplate proxiedRestTemplate;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private Long expiresIn;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        @JsonProperty("avatar_url")
        private String avatarUrl;
        private String name;
    }

    public TokenResponse exchangeCodeForToken(String code, String redirectUri) {
        LinuxDoProperties props = authProperties.getLinuxdo();
        String tokenUrl = props.getTokenUrl();
        String clientId = props.getClientId();
        String clientSecret = props.getClientSecret();

        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = proxiedRestTemplate.postForEntity(tokenUrl, request, TokenResponse.class);
            if (response.getBody() == null) {
                throw new RuntimeException("Failed to exchange code for token: empty response");
            }
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to exchange code for token: {}", e.getMessage());
            throw new RuntimeException("Failed to exchange code for token", e);
        }
    }

    public UserInfo getUserInfo(String accessToken) {
        LinuxDoProperties props = authProperties.getLinuxdo();
        String profileUrl = props.getProfileUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<UserInfo> response = proxiedRestTemplate.exchange(
                    profileUrl, HttpMethod.GET, request, UserInfo.class);
            if (response.getBody() == null) {
                throw new RuntimeException("Failed to get user info: empty response");
            }
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to get user info: {}", e.getMessage());
            throw new RuntimeException("Failed to get user info", e);
        }
    }
}
