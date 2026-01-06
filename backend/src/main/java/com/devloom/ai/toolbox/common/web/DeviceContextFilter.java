package com.devloom.ai.toolbox.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class DeviceContextFilter extends OncePerRequestFilter {

    private static final String HEADER_DEVICE_ID = "X-Device-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            DeviceContextHolder.setDeviceId(request.getHeader(HEADER_DEVICE_ID));
            filterChain.doFilter(request, response);
        } finally {
            DeviceContextHolder.clear();
        }
    }
}
