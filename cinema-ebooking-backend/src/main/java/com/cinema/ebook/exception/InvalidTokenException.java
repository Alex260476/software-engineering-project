package com.cinema.ebook.exception;

/**
 * InvalidTokenException
 * Thrown when verification or password reset token is invalid or expired.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid or expired token.");
    }

    public InvalidTokenException(String message) {
        super(message);
    }
}
