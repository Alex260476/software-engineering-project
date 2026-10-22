package com.cinema.ebook.controller;

import com.cinema.ebook.dto.SentEmailDTO;
import com.cinema.ebook.dto.response.ApiResponse;
import com.cinema.ebook.service.EmailService;
import com.cinema.ebook.util.DataSeeder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * DevController
 * Provides endpoints for inspecting sent emails (verification tokens)
 * and managing test data for the Sprint 2 demo.
 */
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevController {

    private final EmailService emailService;
    private final DataSeeder dataSeeder;

    @GetMapping("/emails")
    public ResponseEntity<ApiResponse<List<SentEmailDTO>>> getSentEmails() {
        return ResponseEntity.ok(ApiResponse.success(emailService.getRecentEmails(), "Recent emails retrieved"));
    }

    @GetMapping("/latest-verification")
    public ResponseEntity<ApiResponse<Map<String, String>>> getLatestVerification(@RequestParam(required = false) String email) {
        String link = emailService.getLatestVerificationLink(email);
        return ResponseEntity.ok(ApiResponse.success(Map.of("actionUrl", link != null ? link : ""), "Latest verification link"));
    }

    @PostMapping("/reset-demo-users")
    public ResponseEntity<ApiResponse<String>> resetDemoUsers() {
        dataSeeder.seedUsers();
        return ResponseEntity.ok(ApiResponse.success("Demo accounts seeded", "Demo data refreshed successfully"));
    }
}
