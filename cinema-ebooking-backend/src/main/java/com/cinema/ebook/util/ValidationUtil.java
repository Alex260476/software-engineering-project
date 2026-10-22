package com.cinema.ebook.util;

import org.springframework.stereotype.Component;

/**
 * ValidationUtil
 * Input validation utilities
 */
@Component
public class ValidationUtil {

    /**
     * Validate email format
     */
    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }

    /**
     * Validate phone number (10 digits)
     */
    public boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String phoneRegex = "^[0-9]{10}$";
        return phone.matches(phoneRegex);
    }

    /**
     * Validate postal code
     */
    public boolean isValidPostalCode(String postalCode) {
        if (postalCode == null) return false;
        String postalRegex = "^[A-Z0-9]{5,10}$";
        return postalCode.matches(postalRegex);
    }

    /**
     * Validate card number length
     */
    public boolean isValidCardNumberFormat(String cardNumber) {
        if (cardNumber == null) return false;
        String digits = cardNumber.replaceAll("\\D", "");
        return digits.length() >= 13 && digits.length() <= 19;
    }

    /**
     * Validate CVV (3 or 4 digits)
     */
    public boolean isValidCVV(String cvv) {
        if (cvv == null) return false;
        return cvv.matches("^[0-9]{3,4}$");
    }

    /**
     * Sanitize input string
     */
    public String sanitizeInput(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("[<>\"']", "");
    }

    /**
     * Validate name field
     */
    public boolean isValidName(String name) {
        if (name == null) return false;
        return name.length() >= 2 && name.length() <= 50;
    }
}
