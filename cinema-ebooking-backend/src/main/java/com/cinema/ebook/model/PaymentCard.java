package com.cinema.ebook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PaymentCard Entity
 * Stores encrypted payment card information
 * Maximum 3 cards per user
 */
@Entity
@Table(name = "payment_cards", indexes = {
    @Index(name = "idx_payment_cards_user", columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCard {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String cardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Card holder name is required")
    private String cardHolderName;

    /**
     * ENCRYPTED: Full card number stored encrypted (AES-256)
     * Format: Only last 4 digits visible to user
     */
    @NotBlank(message = "Card number is required")
    @Column(columnDefinition = "TEXT")
    private String cardNumberEncrypted;

    /**
     * Display: Shows last 4 digits only
     * Example: "****1234"
     */
    @Column(name = "card_last_four")
    private String cardLastFour;

    /**
     * Card expiration month (1-12)
     */
    private int expirationMonth;

    /**
     * Card expiration year (2-digit or 4-digit)
     */
    private int expirationYear;

    /**
     * Card type: VISA, MASTERCARD, AMEX, DISCOVER
     */
    @Column(name = "card_type")
    private String cardType;

    /**
     * CVV ENCRYPTED (if stored, use careful consideration)
     * Recommendation: Do not store CVV, only validate at payment time
     */
    @Column(columnDefinition = "TEXT")
    private String cvvEncrypted;

    /**
     * Is this card set as default for payments?
     */
    private boolean isDefault = false;

    /**
     * Whether card is active/enabled
     */
    private boolean isActive = true;

    // Timestamps
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Utility Methods
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if card is expired
     */
    public boolean isExpired() {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear() % 100; // 2-digit year
        int currentMonth = now.getMonthValue();

        // Card is expired if year is less, or year is same but month is less
        if (expirationYear < currentYear) return true;
        if (expirationYear == currentYear && expirationMonth < currentMonth) return true;

        return false;
    }

    /**
     * Get masked card display (e.g., "Visa ending in 1234")
     */
    public String getMaskedDisplay() {
        String typeDisplay = cardType != null ? cardType.substring(0, 1).toUpperCase() +
                           cardType.substring(1).toLowerCase() : "Card";
        return typeDisplay + " ending in " + cardLastFour;
    }

    /**
     * Mark as default payment method
     */
    public void setAsDefault() {
        this.isDefault = true;
    }

    /**
     * Unset as default
     */
    public void unsetDefault() {
        this.isDefault = false;
    }
}
