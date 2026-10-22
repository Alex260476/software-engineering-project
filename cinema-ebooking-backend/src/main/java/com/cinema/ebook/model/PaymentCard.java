package com.cinema.ebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentCard Model
 * Represents a saved customer payment card with encrypted number and billing address.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCard {
    private String id;
    private String cardholderName;
    private String cardType;
    private String cardNumberEncrypted;
    private String cardLastFour;
    private String expirationDate;
    private Address billingAddress;
    @Builder.Default
    private LocalDateTime addedAt = LocalDateTime.now();
}
