package com.devloom.ai.toolbox.common.util;

import java.security.SecureRandom;

public final class RandomUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private RandomUtil() {
    }

    public static String numericCode(int digits) {
        int bound = (int) Math.pow(10, digits);
        int value = RANDOM.nextInt(bound);
        return String.format("%0" + digits + "d", value);
    }

    public static String randomHex(int bytesLength) {
        byte[] bytes = new byte[bytesLength];
        RANDOM.nextBytes(bytes);
        char[] chars = new char[bytesLength * 2];
        for (int i = 0; i < bytesLength; i++) {
            int v = bytes[i] & 0xFF;
            chars[i * 2] = HEX[v >>> 4];
            chars[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(chars);
    }
}
