package com.cinema.ebook.exception;

/**
 * DuplicateEmailException
 * Thrown when trying to register with an existing email.
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("An account with this email address already exists.");
    }

    public DuplicateEmailException(String message) {
        super(message);
    }
}
