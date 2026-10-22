package com.cinema.ebook.service;

import com.cinema.ebook.dto.SentEmailDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * EmailService
 * Manages email transmissions (confirmation, password reset, profile changes)
 * and maintains an in-memory queue of sent emails for easy inspection during demos.
 */
@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.app-url:http://localhost:5173}")
    private String appUrl;

    @Value("${app.mail.from:noreply@cinemaflex.com}")
    private String fromEmail;

    private final ConcurrentLinkedDeque<SentEmailDTO> sentEmails = new ConcurrentLinkedDeque<>();

    public void sendVerificationEmail(String email, String firstName, String verificationToken) {
        String verificationUrl = appUrl + "/verify-email?token=" + verificationToken;
        String subject = "CinemaFlex - Verify Your Email Address";
        String body = String.format(
                "Hello %s,\n\n" +
                "Thank you for registering with CinemaFlex Cinema E-Booking System!\n\n" +
                "Please verify your email address by clicking the link below:\n" +
                "%s\n\n" +
                "This verification link is valid for 24 hours.\n\n" +
                "If you did not register for this account, please disregard this email.\n\n" +
                "Warm regards,\n" +
                "CinemaFlex Team",
                firstName != null ? firstName : "Moviegoer",
                verificationUrl
        );

        recordAndSend(email, subject, body, verificationUrl, "VERIFICATION");
    }

    public void sendPasswordResetEmail(String email, String firstName, String resetToken) {
        String resetUrl = appUrl + "/reset-password?token=" + resetToken;
        String subject = "CinemaFlex - Password Reset Request";
        String body = String.format(
                "Hello %s,\n\n" +
                "We received a request to reset your CinemaFlex account password.\n\n" +
                "You can reset your password by clicking the link below:\n" +
                "%s\n\n" +
                "This link will expire in 1 hour.\n\n" +
                "If you did not request a password reset, please contact support immediately.\n\n" +
                "Warm regards,\n" +
                "CinemaFlex Team",
                firstName != null ? firstName : "Moviegoer",
                resetUrl
        );

        recordAndSend(email, subject, body, resetUrl, "PASSWORD_RESET");
    }

    public void sendProfileChangeNotification(String email, String firstName, String summary) {
        String subject = "CinemaFlex - Profile Updated";
        String body = String.format(
                "Hello %s,\n\n" +
                "Your CinemaFlex profile information has been successfully updated.\n\n" +
                "Summary of changes:\n%s\n\n" +
                "If you did not authorize this change, please contact customer support immediately.\n\n" +
                "Warm regards,\n" +
                "CinemaFlex Team",
                firstName != null ? firstName : "Moviegoer",
                summary
        );

        recordAndSend(email, subject, body, null, "PROFILE_UPDATE");
    }

    public void sendPaymentCardNotification(String email, String firstName, String action, String cardLastFour) {
        String subject = "CinemaFlex - Payment Method " + action;
        String body = String.format(
                "Hello %s,\n\n" +
                "A payment card ending in %s has been %s on your CinemaFlex account.\n\n" +
                "If you did not make this change, please log in and review your account security immediately.\n\n" +
                "Warm regards,\n" +
                "CinemaFlex Team",
                firstName != null ? firstName : "Moviegoer",
                cardLastFour,
                action.toLowerCase()
        );

        recordAndSend(email, subject, body, null, "CARD_UPDATE");
    }

    private void recordAndSend(String to, String subject, String body, String actionUrl, String type) {
        SentEmailDTO record = SentEmailDTO.builder()
                .to(to)
                .subject(subject)
                .body(body)
                .actionUrl(actionUrl)
                .type(type)
                .sentAt(LocalDateTime.now())
                .build();

        sentEmails.addFirst(record);
        while (sentEmails.size() > 50) {
            sentEmails.pollLast();
        }

        log.info("\n=======================================================\n" +
                 "[EMAIL OUTBOX] To: {}\nSubject: {}\nAction URL: {}\nBody:\n{}\n" +
                 "=======================================================",
                to, subject, actionUrl, body);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
            } catch (Exception e) {
                log.warn("SMTP send skipped/failed (in-memory outbox retained): {}", e.getMessage());
            }
        }
    }

    public List<SentEmailDTO> getRecentEmails() {
        return new ArrayList<>(sentEmails);
    }

    public String getLatestVerificationLink(String email) {
        for (SentEmailDTO mail : sentEmails) {
            if ("VERIFICATION".equals(mail.getType()) && (email == null || email.equalsIgnoreCase(mail.getTo()))) {
                return mail.getActionUrl();
            }
        }
        return null;
    }
}
