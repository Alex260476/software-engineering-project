package com.cinema.ebook.dto;

import com.cinema.ebook.model.Address;
import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * UserDTO
 * Safe representation of a User for API responses (no password hashes or plaintext card numbers).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UserType userType;
    private UserStatus status;
    private boolean promotionSubscription;
    private boolean emailVerified;
    private Address address;
    @Builder.Default
    private List<PaymentCardResponseDTO> paymentCards = new ArrayList<>();
    @Builder.Default
    private List<String> favoriteMovieIds = new ArrayList<>();
}
