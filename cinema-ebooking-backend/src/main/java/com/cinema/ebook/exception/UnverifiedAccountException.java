package com.cinema.ebook.exception;

/**
 * UnverifiedAccountException
 * Thrown when user tries to login with unverified email
 */
public class UnverifiedAccountException extends RuntimeException {

    public UnverifiedAccountException(String message) {
        super(message);
    }

    public UnverifiedAccountException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnverifiedAccountException() {
        super("Account email is not verified. Please check your email for verification link.");
    }
}
