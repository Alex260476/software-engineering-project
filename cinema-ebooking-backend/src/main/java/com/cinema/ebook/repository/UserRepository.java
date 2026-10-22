package com.cinema.ebook.repository;

import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 * Database access for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserIdAndStatus(String userId, UserStatus status);

    List<User> findByStatus(UserStatus status);

    List<User> findByUserTypeAndStatus(String userType, UserStatus status);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token AND u.emailVerificationTokenExpiry > :now")
    Optional<User> findByValidVerificationToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token AND u.passwordResetTokenExpiry > :now")
    Optional<User> findByValidPasswordResetToken(@Param("token") String token, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(u) FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :threshold ORDER BY u.lastLoginAt ASC")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.lastPasswordChangeAt < :threshold AND u.status = 'ACTIVE'")
    List<User> findUsersWithOldPasswords(@Param("threshold") LocalDateTime threshold);
}
