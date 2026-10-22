package com.cinema.ebook.exception;

/**
 * MaxPaymentCardsException
 * Thrown when trying to store more than 3 payment cards (constraint validation).
 */
public class MaxPaymentCardsException extends RuntimeException {
    public MaxPaymentCardsException() {
        super("Cannot store more than 3 payment cards.");
    }

    public MaxPaymentCardsException(String message) {
        super(message);
    }
}
