package com.cinema.ebook.service.impl;

import com.cinema.ebook.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * EmailServiceImpl
 * Implementation of email notifications
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.app-url}")
    private String appUrl;

    @Override
    public void sendVerificationEmail(String email, String firstName, String verificationToken) {
        String verificationLink = appUrl + "/verify-email?token=" + verificationToken;

        String subject = "Email Verification - Cinema E-Booking";
        String body = "Hello " + firstName + ",\n\n" +
                "Thank you for registering with Cinema E-Booking System.\n" +
                "Please click the link below to verify your email address:\n\n" +
                verificationLink + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "Best regards,\n" +
                "Cinema E-Booking Team";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendPasswordResetEmail(String email, String firstName, String resetToken) {
        String resetLink = appUrl + "/reset-password?token=" + resetToken;

        String subject = "Password Reset - Cinema E-Booking";
        String body = "Hello " + firstName + ",\n\n" +
                "We received a request to reset your password.\n" +
                "Click the link below to reset your password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Cinema E-Booking Team";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendProfileChangeNotification(String email, String firstName, String changedFields) {
        String subject = "Profile Updated - Cinema E-Booking";
        String body = "Hello " + firstName + ",\n\n" +
                "Your profile has been updated.\n" +
                "Changed fields: " + changedFields + "\n\n" +
                "If you didn't make this change, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "Cinema E-Booking Team";

        sendEmail(email, subject, body);
    }

    @Override
    public void sendPaymentCardNotification(String email, String firstName, String action, String cardLastFour) {
        String subject = "Payment Card " + action + " - Cinema E-Booking";
        String body = "Hello " + firstName + ",\n\n" +
                "A payment card ending in " + cardLastFour + " has been " + action.toLowerCase() + ".\n\n" +
                "If you didn't make this change, please contact support immediately.\n\n" +
                "Best regards,\n" +
                "Cinema E-Booking Team";

        sendEmail(email, subject, body);
    }

    /**
     * Send email
     */
    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
        } catch (Exception e) {
            // Log error but don't throw - email failure shouldn't break the flow
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}
