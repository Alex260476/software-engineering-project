package com.cinema.ebook.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Address Entity
 * Stores user billing/delivery addresses
 * Maximum 1 address per user
 */
@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_addresses_user", columnList = "user_id", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String addressId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @NotBlank(message = "Street address is required")
    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "apartment_suite")
    private String apartmentSuite; // Optional: Apt, Suite, etc.

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State/Province is required")
    @Column(name = "state_province")
    private String stateProvince;

    @NotBlank(message = "Postal code is required")
    @Column(name = "postal_code")
    @Pattern(regexp = "^[A-Z0-9]{5,10}$", message = "Invalid postal code format")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    @Column(name = "is_billing_address")
    private boolean isBillingAddress = true;

    @Column(name = "is_shipping_address")
    private boolean isShippingAddress = false;

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
     * Get full address string for display
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        sb.append(streetAddress);

        if (apartmentSuite != null && !apartmentSuite.isEmpty()) {
            sb.append(", ").append(apartmentSuite);
        }

        sb.append(", ").append(city).append(", ")
          .append(stateProvince).append(" ")
          .append(postalCode).append(", ")
          .append(country);

        return sb.toString();
    }

    /**
     * Get shortened address for display
     */
    public String getShortAddress() {
        return city + ", " + stateProvince + " " + postalCode;
    }

    /**
     * Mark as billing address
     */
    public void setAsBillingAddress() {
        this.isBillingAddress = true;
    }

    /**
     * Mark as shipping address
     */
    public void setAsShippingAddress() {
        this.isShippingAddress = true;
    }

    /**
     * Mark for both billing and shipping
     */
    public void setAsBoth() {
        this.isBillingAddress = true;
        this.isShippingAddress = true;
    }
}
