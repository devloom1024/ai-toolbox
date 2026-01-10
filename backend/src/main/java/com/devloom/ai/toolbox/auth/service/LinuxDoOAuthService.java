package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.domain.entity.UserAuthEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.entity.UserOauthProfileEntity;
import com.devloom.ai.toolbox.auth.domain.enums.IdentityType;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import com.devloom.ai.toolbox.auth.domain.repository.UserAuthRepository;
import com.devloom.ai.toolbox.auth.domain.repository.UserOauthProfileRepository;
import com.devloom.ai.toolbox.auth.domain.repository.UserRepository;
import com.devloom.ai.toolbox.auth.dto.request.BindLinuxDoRequest;
import com.devloom.ai.toolbox.auth.dto.response.LinuxDoAuthorizeResponse;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.auth.service.support.AuthProperties;
import com.devloom.ai.toolbox.auth.service.support.LinuxDoApiClient;
import com.devloom.ai.toolbox.auth.service.support.LinuxDoApiClient.UserInfo;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.util.RandomUtil;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class LinuxDoOAuthService {

    private final AuthProperties authProperties;
    private final LinuxDoApiClient linuxDoApiClient;
    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserOauthProfileRepository userOauthProfileRepository;
    private final TokenService tokenService;
    private final Clock clock;

    private final Map<String, Instant> stateStore = new ConcurrentHashMap<>();

    public LinuxDoAuthorizeResponse authorize(String redirectUri) {
        AuthProperties.LinuxDoProperties props = authProperties.getLinuxdo();
        validateConfigured(props);
        String state = RandomUtil.randomHex(8);
        stateStore.put(state, clock.instant().plus(props.getStateTtlMinutes(), ChronoUnit.MINUTES));
        String callback = StringUtils.hasText(redirectUri) ? redirectUri : props.getRedirectBaseUrl();
        String authorizeUrl = UriComponentsBuilder.fromUriString(props.getAuthorizeUrl())
                .queryParam("response_type", "code")
                .queryParam("client_id", props.getClientId())
                .queryParam("state", state)
                .queryParam("redirect_uri", callback)
                .build(true)
                .toUriString();
        return LinuxDoAuthorizeResponse.builder()
                .state(state)
                .authorizeUrl(authorizeUrl)
                .build();
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public TokenResponse handleCallback(String code, String state) {
        validateState(state);

        AuthProperties.LinuxDoProperties props = authProperties.getLinuxdo();
        String redirectUri = props.getRedirectBaseUrl();

        LinuxDoApiClient.TokenResponse linuxDoToken = linuxDoApiClient.exchangeCodeForToken(code, redirectUri);
        UserInfo userInfo = linuxDoApiClient.getUserInfo(linuxDoToken.getAccessToken());

        String oauthUserId = String.valueOf(userInfo.getId());
        String nickname = StringUtils.hasText(userInfo.getName()) ? userInfo.getName() : userInfo.getUsername();
        String avatar = StringUtils.hasText(userInfo.getAvatarUrl()) ? userInfo.getAvatarUrl() : "";
        String email = StringUtils.hasText(userInfo.getEmail()) ? userInfo.getEmail() : "";

        UserOauthProfileEntity existingProfile = userOauthProfileRepository
                .findByIdentityTypeAndOauthUserId(IdentityType.LINUX_DO, oauthUserId)
                .orElse(null);

        UserEntity user;
        if (existingProfile == null) {
            user = createUserAndAuth(nickname, avatar, oauthUserId, email, linuxDoToken, userInfo);
        } else {
            user = updateUserAndAuth(existingProfile, nickname, avatar, email, linuxDoToken);
        }

        return tokenService.issueTokenPair(user);
    }

    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void bind(Long userId, BindLinuxDoRequest request) {
        validateState(request.getState());

        AuthProperties.LinuxDoProperties props = authProperties.getLinuxdo();
        String redirectUri = props.getRedirectBaseUrl();

        LinuxDoApiClient.TokenResponse linuxDoToken = linuxDoApiClient.exchangeCodeForToken(request.getCode(), redirectUri);
        UserInfo userInfo = linuxDoApiClient.getUserInfo(linuxDoToken.getAccessToken());

        String oauthUserId = String.valueOf(userInfo.getId());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(BizErrorCode.RESOURCE_NOT_FOUND));

        if (userOauthProfileRepository.existsByIdentityTypeAndOauthUserId(IdentityType.LINUX_DO, oauthUserId)) {
            throw new BizException(BizErrorCode.LINUXDO_ALREADY_BIND);
        }

        UserAuthEntity auth = UserAuthEntity.builder()
                .user(user)
                .identityType(IdentityType.LINUX_DO)
                .identifier(oauthUserId)
                .credential("")
                .verified(true)
                .lastLoginAt(clock.instant())
                .build();
        userAuthRepository.save(auth);

        UserOauthProfileEntity profile = UserOauthProfileEntity.builder()
                .user(user)
                .identityType(IdentityType.LINUX_DO)
                .oauthUserId(oauthUserId)
                .oauthUsername(userInfo.getUsername())
                .oauthEmail(userInfo.getEmail() != null ? userInfo.getEmail() : "")
                .oauthAvatar(userInfo.getAvatarUrl() != null ? userInfo.getAvatarUrl() : "")
                .accessToken(linuxDoToken.getAccessToken())
                .refreshToken(linuxDoToken.getRefreshToken() != null ? linuxDoToken.getRefreshToken() : "")
                .tokenExpiresAt(linuxDoToken.getExpiresIn() != null
                        ? clock.instant().plus(linuxDoToken.getExpiresIn(), ChronoUnit.SECONDS)
                        : null)
                .rawProfile(toRawProfile(userInfo))
                .build();
        userOauthProfileRepository.save(profile);
    }

    private UserEntity createUserAndAuth(
            String nickname, String avatar, String oauthUserId, String email,
            LinuxDoApiClient.TokenResponse linuxDoToken, UserInfo userInfo) {
        UserEntity user = UserEntity.builder()
                .nickname(nickname)
                .avatar(avatar)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);

        UserAuthEntity auth = UserAuthEntity.builder()
                .user(user)
                .identityType(IdentityType.LINUX_DO)
                .identifier(oauthUserId)
                .credential("")
                .verified(true)
                .lastLoginAt(clock.instant())
                .build();
        userAuthRepository.save(auth);

        UserOauthProfileEntity profile = UserOauthProfileEntity.builder()
                .user(user)
                .identityType(IdentityType.LINUX_DO)
                .oauthUserId(oauthUserId)
                .oauthUsername(nickname)
                .oauthEmail(email)
                .oauthAvatar(avatar)
                .accessToken(linuxDoToken.getAccessToken())
                .refreshToken(linuxDoToken.getRefreshToken() != null ? linuxDoToken.getRefreshToken() : "")
                .tokenExpiresAt(linuxDoToken.getExpiresIn() != null
                        ? clock.instant().plus(linuxDoToken.getExpiresIn(), ChronoUnit.SECONDS)
                        : null)
                .rawProfile(toRawProfile(userInfo))
                .build();
        userOauthProfileRepository.save(profile);

        return user;
    }

    private UserEntity updateUserAndAuth(
            UserOauthProfileEntity profile, String nickname, String avatar, String email,
            LinuxDoApiClient.TokenResponse linuxDoToken) {
        UserEntity user = profile.getUser();

        profile.setOauthUsername(nickname);
        profile.setOauthEmail(email);
        profile.setOauthAvatar(avatar);
        profile.setAccessToken(linuxDoToken.getAccessToken());
        profile.setRefreshToken(linuxDoToken.getRefreshToken() != null ? linuxDoToken.getRefreshToken() : profile.getRefreshToken());
        profile.setTokenExpiresAt(linuxDoToken.getExpiresIn() != null
                ? clock.instant().plus(linuxDoToken.getExpiresIn(), ChronoUnit.SECONDS)
                : null);
        userOauthProfileRepository.save(profile);

        userAuthRepository.findByIdentityTypeAndIdentifier(IdentityType.LINUX_DO, profile.getOauthUserId())
                .ifPresent(auth -> {
                    auth.setLastLoginAt(clock.instant());
                    userAuthRepository.save(auth);
                });

        return user;
    }

    private String toRawProfile(UserInfo userInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(userInfo.getId()).append(",");
        sb.append("\"username\":\"").append(escapeJson(userInfo.getUsername())).append("\",");
        if (userInfo.getEmail() != null) {
            sb.append("\"email\":\"").append(escapeJson(userInfo.getEmail())).append("\",");
        }
        if (userInfo.getAvatarUrl() != null) {
            sb.append("\"avatar_url\":\"").append(escapeJson(userInfo.getAvatarUrl())).append("\",");
        }
        if (userInfo.getName() != null) {
            sb.append("\"name\":\"").append(escapeJson(userInfo.getName())).append("\"");
        } else {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");
        return sb.toString();
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private void validateConfigured(AuthProperties.LinuxDoProperties props) {
        if (!StringUtils.hasText(props.getAuthorizeUrl()) || !StringUtils.hasText(props.getClientId())) {
            throw new BizException(BizErrorCode.LINUXDO_NOT_CONFIGURED);
        }
    }

    private void validateState(String state) {
        Instant expireAt = stateStore.remove(state);
        if (expireAt == null || expireAt.isBefore(clock.instant())) {
            throw new BizException(BizErrorCode.INVALID_PARAMETER);
        }
    }
}
