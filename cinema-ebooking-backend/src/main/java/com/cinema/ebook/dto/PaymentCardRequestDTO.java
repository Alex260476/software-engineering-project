package com.cinema.ebook.dto;

import com.cinema.ebook.model.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PaymentCardRequestDTO
 * Request body for adding or editing a payment card.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardRequestDTO {

    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotBlank(message = "Card type is required")
    private String cardType; // Visa, MasterCard, Amex, Discover

    @NotBlank(message = "Expiration date is required (MM/YY)")
    private String expirationDate;

    private Address billingAddress;
}
