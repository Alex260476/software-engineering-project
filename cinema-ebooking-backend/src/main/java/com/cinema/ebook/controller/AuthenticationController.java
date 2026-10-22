package com.cinema.ebook.controller;

import com.cinema.ebook.dto.request.LoginRequestDTO;
import com.cinema.ebook.dto.request.RegistrationRequestDTO;
import com.cinema.ebook.dto.request.PasswordResetRequestDTO;
import com.cinema.ebook.dto.response.AuthResponseDTO;
import com.cinema.ebook.service.interfaces.AuthenticationService;
import com.cinema.ebook.service.interfaces.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AuthenticationController
 * Handles user authentication endpoints
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordResetService passwordResetService;

    /**
     * Register a new user
     * POST /api/v1/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistrationRequestDTO request) {
        AuthResponseDTO response = authenticationService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login user
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authenticationService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Verify email with token
     * POST /api/v1/auth/verify-email
     */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        AuthResponseDTO response = authenticationService.verifyEmail(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Resend verification email
     * POST /api/v1/auth/resend-verification
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        authenticationService.resendVerificationEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Verification email sent. Please check your inbox.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Request password reset
     * POST /api/v1/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        passwordResetService.requestPasswordReset(email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset link sent to your email. Link expires in 1 hour.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Reset password with token
     * POST /api/v1/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        AuthResponseDTO response = passwordResetService.resetPassword(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Logout user
     * POST /api/v1/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // Extract userId from token and call logout
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout successful. Please remove your token from client storage.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
