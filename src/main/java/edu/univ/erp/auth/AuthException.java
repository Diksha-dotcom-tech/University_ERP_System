package edu.univ.erp.auth;

public class AuthException extends Exception {

    /**
     * Constructs a new exception with the specified detail message.
     */
    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}