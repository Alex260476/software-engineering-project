package com.cinema.ebook.service.interfaces;

import com.cinema.ebook.dto.request.LoginRequestDTO;
import com.cinema.ebook.dto.request.RegistrationRequestDTO;
import com.cinema.ebook.dto.response.AuthResponseDTO;

/**
 * AuthenticationService Interface
 * Handles user authentication operations
 */
public interface AuthenticationService {

    /**
     * Register a new user
     */
    AuthResponseDTO register(RegistrationRequestDTO request);

    /**
     * Authenticate user and return JWT token
     */
    AuthResponseDTO login(LoginRequestDTO request);

    /**
     * Logout user (token invalidation)
     */
    void logout(String userId);

    /**
     * Verify user email with token
     */
    AuthResponseDTO verifyEmail(String token);

    /**
     * Resend verification email
     */
    void resendVerificationEmail(String email);

    /**
     * Check if email is already registered
     */
    boolean isEmailRegistered(String email);
}
