package com.cinema.ebook.exception;

public class MaxPaymentCardsException extends RuntimeException {
    public MaxPaymentCardsException() {
        super("Maximum 3 payment cards allowed per user. Please delete one before adding another.");
    }
    public MaxPaymentCardsException(String message) {
        super(message);
    }
}
