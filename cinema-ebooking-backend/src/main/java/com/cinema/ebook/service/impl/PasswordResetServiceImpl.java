package com.cinema.ebook.service.impl;

import com.cinema.ebook.dto.request.PasswordResetRequestDTO;
import com.cinema.ebook.dto.response.AuthResponseDTO;
import com.cinema.ebook.exception.InvalidTokenException;
import com.cinema.ebook.model.User;
import com.cinema.ebook.repository.UserRepository;
import com.cinema.ebook.security.JwtTokenProvider;
import com.cinema.ebook.service.interfaces.EmailService;
import com.cinema.ebook.service.interfaces.PasswordResetService;
import com.cinema.ebook.util.PasswordHashingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordHashingUtil passwordHashingUtil;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        user.setPasswordResetToken(resetToken);
        user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));

        userRepository.save(user);

        emailService.sendPasswordResetEmail(email, user.getFirstName(), resetToken);
    }

    @Override
    public AuthResponseDTO resetPassword(PasswordResetRequestDTO request) {
        validateResetToken(request.getToken());

        User user = userRepository.findByValidPasswordResetToken(request.getToken(), LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));

        user.setPasswordHash(passwordHashingUtil.hashPassword(request.getNewPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetTokenExpiry(null);
        user.setLastPasswordChangeAt(LocalDateTime.now());

        user = userRepository.save(user);

        String jwtToken = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getUserType().toString());

        return AuthResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .token(jwtToken)
                .tokenExpiration(jwtTokenProvider.getTokenExpiration())
                .message("Password reset successful")
                .build();
    }

    @Override
    public void validateResetToken(String token) {
        userRepository.findByValidPasswordResetToken(token, LocalDateTime.now())
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired reset token"));
    }
}
