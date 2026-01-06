package com.devloom.ai.toolbox.auth.service;

import com.devloom.ai.toolbox.auth.dto.request.BindLinuxDoRequest;
import com.devloom.ai.toolbox.auth.dto.response.TokenResponse;
import com.devloom.ai.toolbox.auth.service.support.AuthProperties;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import com.devloom.ai.toolbox.common.util.RandomUtil;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class LinuxDoOAuthService {

    private final AuthProperties authProperties;
    private final Clock clock;

    private final Map<String, Instant> stateStore = new ConcurrentHashMap<>();

    public ResponseEntity<Void> authorize(String redirectUri) {
        AuthProperties.LinuxDoProperties props = authProperties.getLinuxdo();
        validateConfigured(props);
        String state = RandomUtil.randomHex(8);
        stateStore.put(state, clock.instant().plus(props.getStateTtlMinutes(), ChronoUnit.MINUTES));
        String callback = StringUtils.hasText(redirectUri) ? redirectUri : props.getRedirectBaseUrl();
        URI location = UriComponentsBuilder.fromUriString(props.getAuthorizeUrl())
                .queryParam("client_id", props.getClientId())
                .queryParam("state", state)
                .queryParam("redirect_uri", callback)
                .build(true)
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    public TokenResponse handleCallback(String code, String state) {
        validateState(state);
        throw new BizException(BizErrorCode.FEATURE_NOT_IMPLEMENTED);
    }

    public void bind(Long userId, BindLinuxDoRequest request) {
        validateState(request.getState());
        throw new BizException(BizErrorCode.FEATURE_NOT_IMPLEMENTED);
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
