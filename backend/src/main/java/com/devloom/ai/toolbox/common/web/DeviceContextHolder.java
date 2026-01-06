package com.devloom.ai.toolbox.common.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeviceContextHolder {

    private static final ThreadLocal<String> DEVICE_ID = new ThreadLocal<>();

    public static void setDeviceId(@Nullable String deviceId) {
        if (!StringUtils.hasText(deviceId)) {
            DEVICE_ID.remove();
        } else {
            DEVICE_ID.set(deviceId.trim());
        }
    }

    @Nullable
    public static String getDeviceId() {
        return DEVICE_ID.get();
    }

    public static void clear() {
        DEVICE_ID.remove();
    }
}
