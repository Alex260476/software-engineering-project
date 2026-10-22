package com.cinema.ebook.service.interfaces;

/**
 * EmailService Interface
 * Handles email notifications
 */
public interface EmailService {

    void sendVerificationEmail(String email, String firstName, String verificationToken);

    void sendPasswordResetEmail(String email, String firstName, String resetToken);

    void sendProfileChangeNotification(String email, String firstName, String changedFields);

    void sendPaymentCardNotification(String email, String firstName, String action, String cardLastFour);
}
