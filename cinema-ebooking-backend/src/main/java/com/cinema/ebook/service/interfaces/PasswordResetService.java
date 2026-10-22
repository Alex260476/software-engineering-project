package com.cinema.ebook.service.interfaces;

import com.cinema.ebook.dto.request.PasswordResetRequestDTO;
import com.cinema.ebook.dto.response.AuthResponseDTO;

public interface PasswordResetService {

    void requestPasswordReset(String email);

    AuthResponseDTO resetPassword(PasswordResetRequestDTO request);

    void validateResetToken(String token);
}
