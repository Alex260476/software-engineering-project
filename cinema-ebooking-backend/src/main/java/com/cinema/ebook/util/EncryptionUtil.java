package com.cinema.ebook.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * EncryptionUtil
 * Handles AES-256 encryption and decryption for sensitive data
 */
@Component
public class EncryptionUtil {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    @Value("${app.encryption.cipher-algorithm}")
    private String cipherAlgorithm;

    private static final String ALGORITHM = "AES";

    /**
     * Encrypt data using AES
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKeySpec key = new SecretKeySpec(hexStringToByteArray(encryptionKey), 0, 16, ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedData = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt data using AES
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(cipherAlgorithm);
            SecretKeySpec key = new SecretKeySpec(hexStringToByteArray(encryptionKey), 0, 16, ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedData = cipher.doFinal(decodedData);
            return new String(decryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Convert hex string to byte array
     */
    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Generate a mask showing only last 4 characters
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "****" + lastFour;
    }
}
