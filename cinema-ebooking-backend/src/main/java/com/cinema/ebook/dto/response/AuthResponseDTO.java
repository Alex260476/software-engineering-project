package com.cinema.ebook.dto.response;

import com.cinema.ebook.model.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AuthResponseDTO - NEW for Sprint 2
 * Response DTO for authentication endpoints
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private UserType userRole;
    private String token;
    private long tokenExpiration; // milliseconds
    private String tokenType = "Bearer";
    private boolean emailVerified;
    private LocalDateTime lastLogin;
    private String message;

    public String getAuthorizationHeader() {
        return tokenType + " " + token;
    }
}
