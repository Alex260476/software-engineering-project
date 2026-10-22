package com.cinema.ebook.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * PasswordHashingUtil
 * Securely hashes and verifies passwords using BCrypt.
 */
@Component
public class PasswordHashingUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public String hashPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return encoder.encode(rawPassword);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }

    public boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*(),.?\":{}|<>_].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public String getPasswordStrengthMessage(String password) {
        if (password == null || password.isBlank()) {
            return "Password is required";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters long";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must include at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must include at least one lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password must include at least one digit";
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>_].*")) {
            return "Password must include at least one special character (!@#$%^&*)";
        }
        return "Password is strong";
    }
}
