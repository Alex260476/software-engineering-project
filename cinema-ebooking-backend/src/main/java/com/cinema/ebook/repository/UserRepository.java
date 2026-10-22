package com.cinema.ebook.repository;

import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 * MongoDB repository for User collection operations.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String emailVerificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    List<User> findByUserType(UserType userType);
}
