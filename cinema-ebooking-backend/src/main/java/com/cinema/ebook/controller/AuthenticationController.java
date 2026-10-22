package com.cinema.ebook.controller;

import com.cinema.ebook.dto.AuthResponseDTO;
import com.cinema.ebook.dto.LoginRequestDTO;
import com.cinema.ebook.dto.PasswordResetRequestDTO;
import com.cinema.ebook.dto.RegistrationRequestDTO;
import com.cinema.ebook.dto.response.ApiResponse;
import com.cinema.ebook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthenticationController
 * Endpoints for Registration, Login, Email Verification, Password Reset, and Logout.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    /**
     * TC1: Registration
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(@Valid @RequestBody RegistrationRequestDTO request) {
        AuthResponseDTO result = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(result, "Account created successfully. Please check your email to verify your account."));
    }

    /**
     * TC2 & TC4 & TC5 & TC6: Login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO result = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Login successful"));
    }

    /**
     * TC3: Email verification via POST
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyEmail(@RequestParam String token) {
        AuthResponseDTO result = userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(result, "Email verified successfully"));
    }

    /**
     * TC3: Email verification via GET (for direct browser link clicks)
     */
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> verifyEmailGet(@RequestParam String token) {
        AuthResponseDTO result = userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(result, "Email verified successfully"));
    }

    /**
     * Resend verification email
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        userService.resendVerification(email);
        return ResponseEntity.ok(ApiResponse.success("Verification email resent", "Verification email sent. Please check your inbox."));
    }

    /**
     * Forgot password - sends reset link
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        userService.requestPasswordReset(email);
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", "If the email is registered, a password reset link has been sent."));
    }

    /**
     * Reset password with token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> resetPassword(@Valid @RequestBody PasswordResetRequestDTO request) {
        AuthResponseDTO result = userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(result, "Password has been reset successfully"));
    }

    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Map<String, String>>> logout() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Logged out successfully"), "Logout successful"));
    }
}
