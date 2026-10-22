package com.cinema.ebook.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("Email address already registered. Please use a different email or login.");
    }
    public DuplicateEmailException(String message) {
        super(message);
    }
}
