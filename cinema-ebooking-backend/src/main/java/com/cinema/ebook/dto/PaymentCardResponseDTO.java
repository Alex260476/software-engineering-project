package com.cinema.ebook.dto;

import com.cinema.ebook.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentCardResponseDTO
 * Returns card information to the client with masked card number.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardResponseDTO {
    private String id;
    private String cardholderName;
    private String cardType;
    private String cardLastFour;
    private String expirationDate;
    private Address billingAddress;
    private LocalDateTime addedAt;
}
