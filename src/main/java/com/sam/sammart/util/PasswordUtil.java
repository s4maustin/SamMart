package com.sam.sammart.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * bcrypt hashing for stored credentials. Never log raw or hashed secrets.
 */
public final class PasswordUtil {
    private PasswordUtil() {
    }

    /**
     * Hashes a plaintext password with bcrypt cost 10.
     */
    public static String hash(String plaintext) {
        return BCrypt.hashpw(plaintext, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plaintext password against a stored bcrypt hash.
     */
    public static boolean verify(String plaintext, String hash) {
        if (plaintext == null || hash == null) {
            return false;
        }
        return BCrypt.checkpw(plaintext, hash);
    }
}
