package com.cinema.ebook.service.impl;

import com.cinema.ebook.dto.request.LoginRequestDTO;
import com.cinema.ebook.dto.request.RegistrationRequestDTO;
import com.cinema.ebook.dto.response.AuthResponseDTO;
import com.cinema.ebook.exception.DuplicateEmailException;
import com.cinema.ebook.exception.UnverifiedAccountException;
import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import com.cinema.ebook.repository.UserRepository;
import com.cinema.ebook.security.JwtTokenProvider;
import com.cinema.ebook.service.interfaces.AuthenticationService;
import com.cinema.ebook.service.interfaces.EmailService;
import com.cinema.ebook.util.PasswordHashingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AuthenticationServiceImpl
 * Implementation of authentication operations
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordHashingUtil passwordHashingUtil;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    public AuthResponseDTO register(RegistrationRequestDTO request) {
        // Check if email already exists
        if (isEmailRegistered(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        // Create new user
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordHashingUtil.hashPassword(request.getPassword()));
        user.setUserType(UserType.CUSTOMER);
        user.setStatus(UserStatus.INACTIVE);
        user.setPromotionSubscription(request.isSubscribeToPromotions());

        // Generate verification token
        String verificationToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        user.setEmailVerified(false);

        user = userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verificationToken);

        return AuthResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userRole(user.getUserType())
                .message("Registration successful. Please check your email to verify your account.")
                .build();
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Verify password
        if (!passwordHashingUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        // Check if email is verified
        if (!user.isEmailVerified()) {
            throw new UnverifiedAccountException();
        }

        // Check if account is active
        if (!user.canLogin()) {
            throw new BadCredentialsException("Account is not active. Please contact support.");
        }

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getUserType().toString());

        return AuthResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userRole(user.getUserType())
                .token(token)
                .tokenExpiration(jwtTokenProvider.getTokenExpiration())
                .emailVerified(user.isEmailVerified())
                .lastLogin(user.getLastLoginAt())
                .message("Login successful")
                .build();
    }

    @Override
    public void logout(String userId) {
        // In JWT, logout is handled client-side by removing the token
        // Server-side: could implement token blacklisting if needed
    }

    @Override
    public AuthResponseDTO verifyEmail(String token) {
        User user = userRepository.findByValidVerificationToken(token, LocalDateTime.now())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationTokenExpiry(null);

        user = userRepository.save(user);

        // Generate JWT token for auto-login
        String jwtToken = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getUserType().toString());

        return AuthResponseDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userRole(user.getUserType())
                .token(jwtToken)
                .tokenExpiration(jwtTokenProvider.getTokenExpiration())
                .emailVerified(true)
                .message("Email verified successfully")
                .build();
    }

    @Override
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadCredentialsException("Email is already verified");
        }

        // Generate new token
        String newToken = UUID.randomUUID().toString();
        user.setEmailVerificationToken(newToken);
        user.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        userRepository.save(user);

        // Send verification email
        emailService.sendVerificationEmail(email, user.getFirstName(), newToken);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }
}
