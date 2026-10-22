package com.cinema.ebook.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentCardRequestDTO
 * Request DTO for adding/updating payment cards
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardRequestDTO {

    @NotBlank(message = "Card holder name is required")
    @Size(min = 3, max = 100, message = "Card holder name must be between 3 and 100 characters")
    private String cardHolderName;

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Card number must be between 13-19 digits")
    private String cardNumber;

    @Min(value = 1, message = "Expiration month must be between 1-12")
    @Max(value = 12, message = "Expiration month must be between 1-12")
    private int expirationMonth;

    @Min(value = 2024, message = "Expiration year must be current year or later")
    private int expirationYear;

    @NotBlank(message = "CVV is required")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV must be 3 or 4 digits")
    private String cvv;

    @NotBlank(message = "Card type is required")
    @Pattern(regexp = "^(VISA|MASTERCARD|AMEX|DISCOVER)$", message = "Invalid card type")
    private String cardType;

    private boolean setAsDefault = false;

    /**
     * Validate card expiration
     */
    @AssertTrue(message = "Card has expired or expiration date is invalid")
    private boolean isValidExpiration() {
        if (expirationYear == 0 || expirationMonth == 0) {
            return true; // Will be caught by @Min/@Max
        }

        int currentYear = java.time.LocalDate.now().getYear();
        int currentMonth = java.time.LocalDate.now().getMonthValue();

        // Card must not be expired
        if (expirationYear < currentYear) return false;
        if (expirationYear == currentYear && expirationMonth < currentMonth) return false;

        return true;
    }

    /**
     * Validate Luhn algorithm (basic card number check)
     */
    @AssertTrue(message = "Invalid card number (Luhn check failed)")
    private boolean isValidCardNumber() {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return true; // Will be caught by @NotBlank
        }
        return luhnCheck(cardNumber);
    }

    /**
     * Luhn algorithm for card validation
     */
    private boolean luhnCheck(String cardNumber) {
        // Remove any non-digit characters
        String digits = cardNumber.replaceAll("\\D", "");

        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean isEven = false;

        for (int i = digits.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(String.valueOf(digits.charAt(i)));

            if (isEven) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEven = !isEven;
        }

        return sum % 10 == 0;
    }
}
