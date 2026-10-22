package com.cinema.ebook.service;

import com.cinema.ebook.dto.*;
import com.cinema.ebook.exception.*;
import com.cinema.ebook.model.Address;
import com.cinema.ebook.model.Movie;
import com.cinema.ebook.model.PaymentCard;
import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import com.cinema.ebook.repository.MovieRepository;
import com.cinema.ebook.repository.UserRepository;
import com.cinema.ebook.util.EncryptionUtil;
import com.cinema.ebook.util.PasswordHashingUtil;
import com.cinema.ebook.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * UserService
 * Implements business logic for registration, authentication, profile management,
 * payment card constraints, favorites, and admin user overview.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final PasswordHashingUtil passwordHashingUtil;
    private final EncryptionUtil encryptionUtil;
    private final EmailService emailService;
    private final TokenService tokenService;

    /**
     * TC1: Register user account
     * Default state: INACTIVE, unverified, confirmation email sent.
     */
    public AuthResponseDTO register(RegistrationRequestDTO request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new DuplicateEmailException();
        }

        if (!passwordHashingUtil.isPasswordStrong(request.getPassword())) {
            throw new IllegalArgumentException(passwordHashingUtil.getPasswordStrengthMessage(request.getPassword()));
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(cleanEmail)
                .passwordHash(passwordHashingUtil.hashPassword(request.getPassword()))
                .userType(UserType.CUSTOMER)
                .status(UserStatus.INACTIVE) // Requirement: Inactive upon registration
                .promotionSubscription(request.isPromotionSubscription())
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .address(request.getAddress())
                .paymentCards(new ArrayList<>())
                .favoriteMovieIds(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // If optional payment card is supplied during registration
        if (request.getPaymentCard() != null && request.getPaymentCard().getCardNumber() != null && !request.getPaymentCard().getCardNumber().isBlank()) {
            PaymentCardRequestDTO cardReq = request.getPaymentCard();
            PaymentCard card = PaymentCard.builder()
                    .id(UUID.randomUUID().toString())
                    .cardholderName(cardReq.getCardholderName())
                    .cardType(cardReq.getCardType())
                    .cardNumberEncrypted(encryptionUtil.encrypt(cardReq.getCardNumber()))
                    .cardLastFour(encryptionUtil.maskCardNumber(cardReq.getCardNumber()))
                    .expirationDate(cardReq.getExpirationDate())
                    .billingAddress(cardReq.getBillingAddress() != null ? cardReq.getBillingAddress() : request.getAddress())
                    .addedAt(LocalDateTime.now())
                    .build();
            user.addPaymentCard(card);
        }

        User savedUser = userRepository.save(user);

        // Send confirmation email
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFirstName(), verificationToken);

        return AuthResponseDTO.builder()
                .token(null) // User is NOT logged in upon registration
                .user(UserMapper.toUserDTO(savedUser))
                .emailVerified(false)
                .message("Account created successfully. Confirmation email sent.")
                .build();
    }

    /**
     * TC2 & TC4 & TC5 & TC6: Login
     * Validates credentials, checks verification status and role.
     */
    public AuthResponseDTO login(LoginRequestDTO request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(InvalidCredentialsException::new);

        // Check password hash
        if (!passwordHashingUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // TC2 Requirement: Check verification status
        if (!user.isEmailVerified() || user.getStatus() == UserStatus.INACTIVE) {
            throw new UnverifiedAccountException();
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new InvalidCredentialsException("Account is suspended. Please contact customer support.");
        }

        // Update login stats
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = tokenService.generateToken(user);

        return AuthResponseDTO.builder()
                .token(token)
                .user(UserMapper.toUserDTO(user))
                .emailVerified(true)
                .message("Login successful")
                .build();
    }

    /**
     * TC3: Verify Account via token from email link
     */
    public AuthResponseDTO verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Verification token is required");
        }

        User user = userRepository.findByEmailVerificationToken(token.trim())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired verification token"));

        if (user.getEmailVerificationTokenExpiry() != null &&
                user.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Generate token for auto-login after verification
        String authToken = tokenService.generateToken(savedUser);

        return AuthResponseDTO.builder()
                .token(authToken)
                .user(UserMapper.toUserDTO(savedUser))
                .emailVerified(true)
                .message("Email verified successfully! Your account is now Active.")
                .build();
    }

    public void resendVerification(String email) {
        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Account is already verified.");
        }

        String newToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(newToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), newToken);
    }

    public void requestPasswordReset(String email) {
        String cleanEmail = email.trim().toLowerCase();
        userRepository.findByEmail(cleanEmail).ifPresent(user -> {
            String resetToken = UUID.randomUUID().toString();
            user.setPasswordResetToken(resetToken);
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetToken);
        });
    }

    public AuthResponseDTO resetPassword(PasswordResetRequestDTO request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new InvalidTokenException("Reset token is required");
        }

        User user = userRepository.findByPasswordResetToken(request.getToken().trim())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

        if (user.getPasswordResetTokenExpiry() != null &&
                user.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Password reset link has expired. Please request a new one.");
        }

        if (!passwordHashingUtil.isPasswordStrong(request.getNewPassword())) {
            throw new IllegalArgumentException(passwordHashingUtil.getPasswordStrengthMessage(request.getNewPassword()));
        }

        user.setPasswordHash(passwordHashingUtil.hashPassword(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setLastPasswordChangeAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return AuthResponseDTO.builder()
                .token(tokenService.generateToken(savedUser))
                .user(UserMapper.toUserDTO(savedUser))
                .emailVerified(savedUser.isEmailVerified())
                .message("Password has been reset successfully. You can now log in.")
                .build();
    }

    public UserDTO getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        return UserMapper.toUserDTO(user);
    }

    /**
     * TC7: Edit Profile
     * Email address cannot be modified.
     * Restriction of max 1 address enforced.
     * Sends email notification upon profile update.
     */
    public UserDTO updateProfile(String userId, UpdateProfileRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<String> changedFields = new ArrayList<>();

        if (!Objects.equals(user.getFirstName(), request.getFirstName())) {
            changedFields.add("First Name");
            user.setFirstName(request.getFirstName());
        }
        if (!Objects.equals(user.getLastName(), request.getLastName())) {
            changedFields.add("Last Name");
            user.setLastName(request.getLastName());
        }
        if (!Objects.equals(user.getPhone(), request.getPhone())) {
            changedFields.add("Phone Number");
            user.setPhone(request.getPhone());
        }
        if (user.isPromotionSubscription() != request.isPromotionSubscription()) {
            changedFields.add("Promotion Subscription (" + (request.isPromotionSubscription() ? "Subscribed" : "Unsubscribed") + ")");
            user.setPromotionSubscription(request.isPromotionSubscription());
        }

        if (request.getAddress() != null) {
            Address newAddr = request.getAddress();
            Address oldAddr = user.getAddress();
            if (oldAddr == null || !Objects.equals(oldAddr.getStreet(), newAddr.getStreet()) ||
                    !Objects.equals(oldAddr.getCity(), newAddr.getCity()) ||
                    !Objects.equals(oldAddr.getState(), newAddr.getState()) ||
                    !Objects.equals(oldAddr.getZipCode(), newAddr.getZipCode())) {
                changedFields.add("Home Address");
                user.setAddress(newAddr);
            }
        }

        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        if (!changedFields.isEmpty()) {
            emailService.sendProfileChangeNotification(
                    savedUser.getEmail(),
                    savedUser.getFirstName(),
                    String.join(", ", changedFields)
            );
        }

        return UserMapper.toUserDTO(savedUser);
    }

    /**
     * Password change requires current password per security requirements.
     */
    public void changePassword(String userId, ChangePasswordRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!passwordHashingUtil.verifyPassword(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password does not match.");
        }

        if (!passwordHashingUtil.isPasswordStrong(request.getNewPassword())) {
            throw new IllegalArgumentException(passwordHashingUtil.getPasswordStrengthMessage(request.getNewPassword()));
        }

        user.setPasswordHash(passwordHashingUtil.hashPassword(request.getNewPassword()));
        user.setLastPasswordChangeAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        emailService.sendProfileChangeNotification(
                user.getEmail(),
                user.getFirstName(),
                "Account Password"
        );
    }

    /**
     * TC7: Add payment card. Constraint: cannot exceed 3 payment cards.
     * Card number is encrypted using AES before saving to DB.
     */
    public UserDTO addPaymentCard(String userId, PaymentCardRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getPaymentCards() != null && user.getPaymentCards().size() >= 3) {
            throw new MaxPaymentCardsException("Users cannot store more than 3 payment cards.");
        }

        String cleanCardNumber = request.getCardNumber().replaceAll("\\s+", "");
        if (cleanCardNumber.length() < 12) {
            throw new IllegalArgumentException("Card number is invalid");
        }

        PaymentCard card = PaymentCard.builder()
                .id(UUID.randomUUID().toString())
                .cardholderName(request.getCardholderName().trim())
                .cardType(request.getCardType() != null ? request.getCardType() : "Card")
                .cardNumberEncrypted(encryptionUtil.encrypt(cleanCardNumber)) // Encrypted with AES!
                .cardLastFour(encryptionUtil.maskCardNumber(cleanCardNumber))
                .expirationDate(request.getExpirationDate().trim())
                .billingAddress(request.getBillingAddress() != null ? request.getBillingAddress() : user.getAddress())
                .addedAt(LocalDateTime.now())
                .build();

        user.addPaymentCard(card);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        emailService.sendPaymentCardNotification(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                "Added",
                card.getCardLastFour()
        );

        return UserMapper.toUserDTO(savedUser);
    }

    /**
     * TC7: Delete payment card.
     */
    public UserDTO deletePaymentCard(String userId, String cardId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getPaymentCards() == null) {
            throw new ResourceNotFoundException("No payment cards found");
        }

        PaymentCard removedCard = user.getPaymentCards().stream()
                .filter(c -> Objects.equals(c.getId(), cardId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with ID: " + cardId));

        user.getPaymentCards().remove(removedCard);
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        emailService.sendPaymentCardNotification(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                "Removed",
                removedCard.getCardLastFour()
        );

        return UserMapper.toUserDTO(savedUser);
    }

    /**
     * TC7: Update existing payment card.
     */
    public UserDTO updatePaymentCard(String userId, String cardId, PaymentCardRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        PaymentCard existing = user.getPaymentCards().stream()
                .filter(c -> Objects.equals(c.getId(), cardId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with ID: " + cardId));

        if (request.getCardholderName() != null) {
            existing.setCardholderName(request.getCardholderName().trim());
        }
        if (request.getCardType() != null) {
            existing.setCardType(request.getCardType());
        }
        if (request.getExpirationDate() != null) {
            existing.setExpirationDate(request.getExpirationDate().trim());
        }
        if (request.getBillingAddress() != null) {
            existing.setBillingAddress(request.getBillingAddress());
        }
        if (request.getCardNumber() != null && !request.getCardNumber().contains("*")) {
            String clean = request.getCardNumber().replaceAll("\\s+", "");
            existing.setCardNumberEncrypted(encryptionUtil.encrypt(clean));
            existing.setCardLastFour(encryptionUtil.maskCardNumber(clean));
        }

        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);

        emailService.sendPaymentCardNotification(
                savedUser.getEmail(),
                savedUser.getFirstName(),
                "Updated",
                existing.getCardLastFour()
        );

        return UserMapper.toUserDTO(savedUser);
    }

    /**
     * TC8 & TC9: Favorites
     */
    public List<Movie> getFavoriteMovies(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getFavoriteMovieIds() == null || user.getFavoriteMovieIds().isEmpty()) {
            return new ArrayList<>();
        }

        return movieRepository.findAllById(user.getFavoriteMovieIds());
    }

    public UserDTO addFavoriteMovie(String userId, String movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie not found: " + movieId);
        }

        if (user.getFavoriteMovieIds() == null) {
            user.setFavoriteMovieIds(new ArrayList<>());
        }

        if (!user.getFavoriteMovieIds().contains(movieId)) {
            user.getFavoriteMovieIds().add(movieId);
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        return UserMapper.toUserDTO(user);
    }

    public UserDTO removeFavoriteMovie(String userId, String movieId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (user.getFavoriteMovieIds() != null && user.getFavoriteMovieIds().remove(movieId)) {
            user.setUpdatedAt(LocalDateTime.now());
            user = userRepository.save(user);
        }

        return UserMapper.toUserDTO(user);
    }

    /**
     * Admin Portal: Get all users for admin management prototype
     */
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDTO)
                .collect(Collectors.toList());
    }
}
