package com.cinema.ebook.controller;

import com.cinema.ebook.dto.*;
import com.cinema.ebook.dto.response.ApiResponse;
import com.cinema.ebook.model.Movie;
import com.cinema.ebook.service.TokenService;
import com.cinema.ebook.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * UserController
 * Endpoints for Profile editing, Payment Card management, and Favorite Movies.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;

    private String resolveUserId(String authHeader, String fallbackUserId) {
        if (authHeader != null && !authHeader.isBlank()) {
            String uid = tokenService.validateTokenAndGetUserId(authHeader);
            if (uid != null) return uid;
        }
        if (fallbackUserId != null && !fallbackUserId.isBlank()) {
            return fallbackUserId.trim();
        }
        throw new IllegalArgumentException("Authentication required. Please provide a valid Authorization token.");
    }

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile, "Profile retrieved successfully"));
    }

    /**
     * TC7: Update user profile (email is non-modifiable; sends notification)
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDTO>> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Profile updated successfully"));
    }

    /**
     * Change password (requires current password per security requirements)
     */
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(Map.of("message", "Password changed successfully"), "Password changed successfully"));
    }

    /**
     * TC7: Add payment card (constraint: max 3 cards; encrypted in DB)
     */
    @PostMapping("/cards")
    public ResponseEntity<ApiResponse<UserDTO>> addPaymentCard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @Valid @RequestBody PaymentCardRequestDTO request) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.addPaymentCard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(updated, "Payment card added successfully"));
    }

    /**
     * TC7: Update payment card
     */
    @PutMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<UserDTO>> updatePaymentCard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @PathVariable String cardId,
            @Valid @RequestBody PaymentCardRequestDTO request) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.updatePaymentCard(userId, cardId, request);
        return ResponseEntity.ok(ApiResponse.success(updated, "Payment card updated successfully"));
    }

    /**
     * TC7: Delete payment card
     */
    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<ApiResponse<UserDTO>> deletePaymentCard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @PathVariable String cardId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.deletePaymentCard(userId, cardId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Payment card removed successfully"));
    }

    /**
     * TC8 & TC9: Get user's favorite movies list
     */
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<Movie>>> getFavorites(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        List<Movie> favorites = userService.getFavoriteMovies(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites, "Favorites retrieved successfully"));
    }

    /**
     * TC8: Add movie to favorites from browsing page
     */
    @PostMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<UserDTO>> addFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @PathVariable String movieId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.addFavoriteMovie(userId, movieId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Movie added to favorites"));
    }

    /**
     * Remove movie from favorites
     */
    @DeleteMapping("/favorites/{movieId}")
    public ResponseEntity<ApiResponse<UserDTO>> removeFavorite(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId,
            @PathVariable String movieId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO updated = userService.removeFavoriteMovie(userId, movieId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Movie removed from favorites"));
    }

    /**
     * Admin Portal: list all users
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "userId", required = false) String fallbackUserId) {
        String userId = resolveUserId(authHeader, fallbackUserId);
        UserDTO requestingUser = userService.getUserProfile(userId);
        if (!"ADMIN".equalsIgnoreCase(String.valueOf(requestingUser.getUserType()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied: Admin role required", "FORBIDDEN"));
        }
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }
}
