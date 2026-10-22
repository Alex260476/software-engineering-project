package com.cinema.ebook.util;

import com.cinema.ebook.dto.PaymentCardResponseDTO;
import com.cinema.ebook.dto.UserDTO;
import com.cinema.ebook.model.PaymentCard;
import com.cinema.ebook.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserMapper
 * Maps between User / PaymentCard entities and their DTOs.
 */
public final class UserMapper {

    private UserMapper() {}

    public static PaymentCardResponseDTO toCardDTO(PaymentCard card) {
        if (card == null) return null;
        return PaymentCardResponseDTO.builder()
                .id(card.getId())
                .cardholderName(card.getCardholderName())
                .cardType(card.getCardType())
                .cardLastFour(card.getCardLastFour())
                .expirationDate(card.getExpirationDate())
                .billingAddress(card.getBillingAddress())
                .addedAt(card.getAddedAt())
                .build();
    }

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;

        List<PaymentCardResponseDTO> cardDTOs = new ArrayList<>();
        if (user.getPaymentCards() != null) {
            cardDTOs = user.getPaymentCards().stream()
                    .map(UserMapper::toCardDTO)
                    .collect(Collectors.toList());
        }

        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .userType(user.getUserType())
                .status(user.getStatus())
                .promotionSubscription(user.isPromotionSubscription())
                .emailVerified(user.isEmailVerified())
                .address(user.getAddress())
                .paymentCards(cardDTOs)
                .favoriteMovieIds(user.getFavoriteMovieIds() != null ? new ArrayList<>(user.getFavoriteMovieIds()) : new ArrayList<>())
                .build();
    }
}
