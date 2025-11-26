package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing helper using jBCrypt only.
 */
public class PasswordHasher {

    private PasswordHasher() {
        // utility class
    }

    /** Hash a plain password using BCrypt. */
    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /** Verify a plain password against a stored BCrypt hash. */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // malformed hash => treat as invalid
            return false;
        }
    }
}
