package com.devloom.ai.toolbox.common.security;

import com.devloom.ai.toolbox.auth.domain.entity.UserEntity;
import com.devloom.ai.toolbox.auth.domain.enums.UserStatus;
import com.devloom.ai.toolbox.auth.domain.repository.UserRepository;
import com.devloom.ai.toolbox.common.exception.BizErrorCode;
import com.devloom.ai.toolbox.common.exception.BizException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            JwtTokenProvider.JwtPayload payload = jwtTokenProvider.parse(token);
            Optional<UserEntity> userOptional = userRepository.findById(payload.userId());
            if (userOptional.isEmpty()) {
                throw new BizException(BizErrorCode.UNAUTHORIZED);
            }
            UserEntity user = userOptional.get();
            if (user.getStatus() == UserStatus.DELETED) {
                throw new BizException(BizErrorCode.UNAUTHORIZED);
            }
            if (user.getStatus() == UserStatus.LOCKED) {
                throw new BizException(BizErrorCode.ACCOUNT_LOCKED);
            }
            CurrentUser principal = new CurrentUser(user.getId(), user.getNickname(), user.getStatus());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
