package com.cinema.ebook.model;

import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity
 * Favorites are tracked through the FavoriteMovie entity, not here.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String userId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType; // CUSTOMER, ADMIN

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.INACTIVE;

    @Column(name = "promotion_subscription")
    private boolean promotionSubscription = false;

    @Column(name = "email_verification_token", length = 500)
    private String emailVerificationToken;

    @Column(name = "email_verification_token_expiry")
    private LocalDateTime emailVerificationTokenExpiry;

    @Column(name = "password_reset_token", length = 500)
    private String passwordResetToken;

    @Column(name = "password_reset_token_expiry")
    private LocalDateTime passwordResetTokenExpiry;

    @Column(name = "is_email_verified")
    private boolean emailVerified = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "last_password_change_at")
    private LocalDateTime lastPasswordChangeAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentCard> paymentCards = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Address address;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canLogin() {
        return status != null && status.canLogin() && emailVerified;
    }

    public boolean hasMaxPaymentCards() {
        return paymentCards != null && paymentCards.size() >= 3;
    }

    public boolean isAdmin() {
        return UserType.ADMIN.equals(userType);
    }

    public boolean isCustomer() {
        return UserType.CUSTOMER.equals(userType);
    }

    public void addPaymentCard(PaymentCard card) {
        if (hasMaxPaymentCards()) {
            throw new IllegalStateException("Maximum payment cards (3) reached");
        }
        if (paymentCards == null) {
            paymentCards = new ArrayList<>();
        }
        card.setUser(this);
        paymentCards.add(card);
    }
}