package com.cinema.ebook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SentEmailDTO
 * Holds recorded email transmissions for verification link inspection during demos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentEmailDTO {
    private String to;
    private String subject;
    private String body;
    private String actionUrl;
    private String type; // VERIFICATION, PASSWORD_RESET, PROFILE_UPDATE, CARD_UPDATE
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();
}
