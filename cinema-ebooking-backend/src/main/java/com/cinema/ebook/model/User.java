package com.cinema.ebook.model;

import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User Entity
 * Stored in MongoDB "users" collection.
 * Contains user credentials, roles, profile, address, and up to 3 encrypted payment cards.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String firstName;
    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String phone;

    private String passwordHash;

    @Builder.Default
    private UserType userType = UserType.CUSTOMER;

    @Builder.Default
    private UserStatus status = UserStatus.INACTIVE;

    @Builder.Default
    private boolean promotionSubscription = false;

    @Builder.Default
    private boolean emailVerified = false;

    private String emailVerificationToken;
    private LocalDateTime emailVerificationTokenExpiry;

    private String passwordResetToken;
    private LocalDateTime passwordResetTokenExpiry;

    private Address address;

    @Builder.Default
    private List<PaymentCard> paymentCards = new ArrayList<>();

    @Builder.Default
    private List<String> favoriteMovieIds = new ArrayList<>();

    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime lastLoginAt;
    private LocalDateTime lastPasswordChangeAt;

    public boolean canLogin() {
        return status == UserStatus.ACTIVE && emailVerified;
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
            throw new IllegalStateException("Maximum of 3 payment cards allowed.");
        }
        if (paymentCards == null) {
            paymentCards = new ArrayList<>();
        }
        paymentCards.add(card);
    }
}
