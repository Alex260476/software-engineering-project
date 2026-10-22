package com.cinema.ebook.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingAndEncryptionTest {

    private PasswordHashingUtil passwordUtil;
    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        passwordUtil = new PasswordHashingUtil();
        encryptionUtil = new EncryptionUtil("cinemaflex-secret-key-test-2026");
    }

    @Test
    @DisplayName("TC10: Passwords must be securely hashed with BCrypt")
    void testPasswordHashing() {
        String plain = "MyPass!20";
        String hash = passwordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertNotEquals(plain, hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$")); // BCrypt prefix
        assertTrue(passwordUtil.verifyPassword(plain, hash));
        assertFalse(passwordUtil.verifyPassword("WrongPassword!1", hash));
    }

    @Test
    @DisplayName("Security: Password strength requirements (8+ chars, upper, lower, digit, symbol)")
    void testPasswordStrength() {
        assertTrue(passwordUtil.isPasswordStrong("MyPass!20"));
        assertTrue(passwordUtil.isPasswordStrong("AdminPass!20"));

        assertFalse(passwordUtil.isPasswordStrong("short1!"));
        assertFalse(passwordUtil.isPasswordStrong("nouppercase1!"));
        assertFalse(passwordUtil.isPasswordStrong("NOLOWERCASE1!"));
        assertFalse(passwordUtil.isPasswordStrong("NoDigitsHere!"));
        assertFalse(passwordUtil.isPasswordStrong("NoSpecialChar12"));
    }

    @Test
    @DisplayName("TC10: Payment card numbers must be AES encrypted and decrypted")
    void testCardEncryptionAndDecryption() {
        String rawCard = "4111111111111111";
        String encrypted = encryptionUtil.encrypt(rawCard);

        assertNotNull(encrypted);
        assertNotEquals(rawCard, encrypted);

        String decrypted = encryptionUtil.decrypt(encrypted);
        assertEquals(rawCard, decrypted);
    }

    @Test
    @DisplayName("Security: Mask card numbers to only show last 4 digits")
    void testMaskCardNumber() {
        assertEquals("**** **** **** 4242", encryptionUtil.maskCardNumber("4242424242424242"));
        assertEquals("**** **** **** 1111", encryptionUtil.maskCardNumber("4111 1111 1111 1111"));
    }
}
