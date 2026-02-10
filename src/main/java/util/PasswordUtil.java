package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple password hashing utility using SHA-256.
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using SHA-256.
     */
    public static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
