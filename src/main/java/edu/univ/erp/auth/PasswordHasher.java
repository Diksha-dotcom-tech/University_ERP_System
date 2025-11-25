package edu.univ.erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verify(String plainPassword, String hashed) {
        if (hashed == null || hashed.isEmpty()) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashed);
    }
}
