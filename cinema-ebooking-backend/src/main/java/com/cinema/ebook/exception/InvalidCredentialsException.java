package com.cinema.ebook.exception;

/**
 * InvalidCredentialsException
 * Thrown on bad email/password combination.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
