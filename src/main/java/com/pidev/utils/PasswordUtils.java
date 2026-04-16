package com.pidev.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordUtils {

    /**
     * Hashes a plaintext password using SHA‑256 and returns a Base64 string.
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plaintext password against a stored SHA‑256 hash (Base64).
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null) return false;
        String newHash = hashPassword(plainPassword);
        return newHash.equals(storedHash);
    }

    /**
     * Checks if a string looks like a SHA‑256 Base64 hash (44 characters).
     */
    public static boolean isHashed(String password) {
        return password != null && password.length() == 44;
    }

}