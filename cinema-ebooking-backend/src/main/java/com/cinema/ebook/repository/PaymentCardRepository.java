package com.cinema.ebook.repository;

import com.cinema.ebook.model.PaymentCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, String> {

    List<PaymentCard> findByUserId(String userId);

    List<PaymentCard> findByUserIdAndIsActive(String userId, boolean isActive);

    Optional<PaymentCard> findByCardIdAndUserId(String cardId, String userId);

    @Query("SELECT COUNT(pc) FROM PaymentCard pc WHERE pc.user.userId = :userId AND pc.isActive = true")
    long countActiveCardsByUserId(@Param("userId") String userId);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.userId = :userId AND pc.isDefault = true AND pc.isActive = true")
    Optional<PaymentCard> findDefaultCardForUser(@Param("userId") String userId);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.userId = :userId AND pc.isActive = true ORDER BY pc.createdAt DESC")
    List<PaymentCard> findActiveCardsByUserIdOrderByDate(@Param("userId") String userId);
}
