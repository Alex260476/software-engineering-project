package com.cinema.ebook.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PasswordHashingUtil
 * Handles password hashing and verification using BCrypt
 */
@Component
public class PasswordHashingUtil {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    /**
     * Hash a password using BCrypt
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Verify a password against its hash
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Check if password meets requirements
     */
    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[@$!%*?&].*");

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * Get password strength message
     */
    public String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }

        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }

        StringBuilder message = new StringBuilder("Password missing: ");
        boolean missing = false;

        if (!password.matches(".*[A-Z].*")) {
            message.append("uppercase letter ");
            missing = true;
        }
        if (!password.matches(".*[a-z].*")) {
            message.append("lowercase letter ");
            missing = true;
        }
        if (!password.matches(".*\\d.*")) {
            message.append("digit ");
            missing = true;
        }
        if (!password.matches(".*[@$!%*?&].*")) {
            message.append("special character");
            missing = true;
        }

        return missing ? message.toString() : "Password is strong";
    }
}
