package com.haidian.intel.platform.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Password hashing utility based on SHA-256 for the current lightweight auth flow.
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    public static String encode(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(digest.length * 2);
            for (byte currentByte : digest) {
                builder.append(String.format("%02x", currentByte));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}
