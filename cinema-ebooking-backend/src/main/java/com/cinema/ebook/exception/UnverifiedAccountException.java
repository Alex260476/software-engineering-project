package com.cinema.ebook.exception;

/**
 * UnverifiedAccountException
 * Thrown when an unverified user attempts to log in.
 */
public class UnverifiedAccountException extends RuntimeException {
    public UnverifiedAccountException() {
        super("Account is not verified. Please check your email to verify your account.");
    }

    public UnverifiedAccountException(String message) {
        super(message);
    }
}
