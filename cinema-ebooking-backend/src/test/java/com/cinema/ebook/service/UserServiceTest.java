package com.cinema.ebook.service;

import com.cinema.ebook.dto.*;
import com.cinema.ebook.exception.*;
import com.cinema.ebook.model.Address;
import com.cinema.ebook.model.Movie;
import com.cinema.ebook.model.PaymentCard;
import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import com.cinema.ebook.repository.MovieRepository;
import com.cinema.ebook.repository.UserRepository;
import com.cinema.ebook.util.EncryptionUtil;
import com.cinema.ebook.util.PasswordHashingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private EmailService emailService;

    private PasswordHashingUtil passwordHashingUtil;
    private EncryptionUtil encryptionUtil;
    private TokenService tokenService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        passwordHashingUtil = new PasswordHashingUtil();
        encryptionUtil = new EncryptionUtil("cinemaflex-test-key-2026");
        tokenService = new TokenService("cinemaflex-jwt-secret-test-key-2026");
        userService = new UserService(
                userRepository,
                movieRepository,
                passwordHashingUtil,
                encryptionUtil,
                emailService,
                tokenService
        );
    }

    @Test
    @DisplayName("TC1: Registration creates INACTIVE account, unverified, and sends verification email")
    void testTC1_RegisterUser() {
        RegistrationRequestDTO request = RegistrationRequestDTO.builder()
                .firstName("Eman")
                .lastName("Saleh")
                .email("eman@gmail.my")
                .password("MyPass!20")
                .promotionSubscription(true)
                .build();

        when(userRepository.existsByEmail("eman@gmail.my")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("eman-uuid-123");
            return u;
        });

        AuthResponseDTO response = userService.register(request);

        assertNotNull(response);
        assertNull(response.getToken(), "User must NOT be logged in upon registration");
        assertFalse(response.isEmailVerified(), "User must be unverified");
        assertEquals(UserStatus.INACTIVE, response.getUser().getStatus(), "User status must be INACTIVE");

        verify(emailService, times(1)).sendVerificationEmail(eq("eman@gmail.my"), eq("Eman"), anyString());
    }

    @Test
    @DisplayName("TC2: Login with unverified account throws UnverifiedAccountException with exact message")
    void testTC2_LoginUnverifiedAccount() {
        User unverifiedUser = User.builder()
                .id("eman-123")
                .email("eman@gmail.my")
                .passwordHash(passwordHashingUtil.hashPassword("MyPass!20"))
                .status(UserStatus.INACTIVE)
                .emailVerified(false)
                .build();

        when(userRepository.findByEmail("eman@gmail.my")).thenReturn(Optional.of(unverifiedUser));

        UnverifiedAccountException ex = assertThrows(UnverifiedAccountException.class, () ->
                userService.login(new LoginRequestDTO("eman@gmail.my", "MyPass!20"))
        );

        assertEquals("Account is not verified. Please check your email to verify your account.", ex.getMessage());
    }

    @Test
    @DisplayName("TC3: Account verification via email token activates account")
    void testTC3_VerifyAccount() {
        User user = User.builder()
                .id("eman-123")
                .email("eman@gmail.my")
                .status(UserStatus.INACTIVE)
                .emailVerified(false)
                .emailVerificationToken("test-token-xyz")
                .emailVerificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        when(userRepository.findByEmailVerificationToken("test-token-xyz")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponseDTO response = userService.verifyEmail("test-token-xyz");

        assertTrue(response.isEmailVerified());
        assertEquals(UserStatus.ACTIVE, response.getUser().getStatus());
        assertNotNull(response.getToken(), "Token should be issued for auto-login");
    }

    @Test
    @DisplayName("TC4: Login with verified user succeeds")
    void testTC4_LoginVerifiedUser() {
        User activeUser = User.builder()
                .id("sarah-123")
                .email("favuser@gmail.com")
                .firstName("Sarah")
                .lastName("Jenkins")
                .passwordHash(passwordHashingUtil.hashPassword("UserPass!20"))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .userType(UserType.CUSTOMER)
                .build();

        when(userRepository.findByEmail("favuser@gmail.com")).thenReturn(Optional.of(activeUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponseDTO response = userService.login(new LoginRequestDTO("favuser@gmail.com", "UserPass!20"));

        assertNotNull(response.getToken());
        assertEquals("Sarah", response.getUser().getFirstName());
        assertEquals(UserType.CUSTOMER, response.getUser().getUserType());
    }

    @Test
    @DisplayName("TC5: Login with incorrect password is denied")
    void testTC5_InvalidLoginAttempt() {
        User activeUser = User.builder()
                .id("sarah-123")
                .email("favuser@gmail.com")
                .passwordHash(passwordHashingUtil.hashPassword("UserPass!20"))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();

        when(userRepository.findByEmail("favuser@gmail.com")).thenReturn(Optional.of(activeUser));

        assertThrows(InvalidCredentialsException.class, () ->
                userService.login(new LoginRequestDTO("favuser@gmail.com", "WrongPassword!99"))
        );
    }

    @Test
    @DisplayName("TC6: Login with Admin account returns ADMIN role")
    void testTC6_LoginAdminAccount() {
        User adminUser = User.builder()
                .id("admin-123")
                .email("admin@cinema.com")
                .firstName("Admin")
                .lastName("User")
                .passwordHash(passwordHashingUtil.hashPassword("AdminPass!20"))
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .userType(UserType.ADMIN)
                .build();

        when(userRepository.findByEmail("admin@cinema.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponseDTO response = userService.login(new LoginRequestDTO("admin@cinema.com", "AdminPass!20"));

        assertNotNull(response.getToken());
        assertEquals(UserType.ADMIN, response.getUser().getUserType());
    }

    @Test
    @DisplayName("TC7: System enforces max 3 payment cards constraint")
    void testTC7_MaxPaymentCardsConstraint() {
        List<PaymentCard> threeCards = new ArrayList<>(List.of(
                PaymentCard.builder().id("1").cardLastFour("1111").build(),
                PaymentCard.builder().id("2").cardLastFour("2222").build(),
                PaymentCard.builder().id("3").cardLastFour("3333").build()
        ));

        User userWith3Cards = User.builder()
                .id("alex-123")
                .email("cardsuser@gmail.com")
                .paymentCards(threeCards)
                .build();

        when(userRepository.findById("alex-123")).thenReturn(Optional.of(userWith3Cards));

        PaymentCardRequestDTO fourthCard = PaymentCardRequestDTO.builder()
                .cardholderName("Alex Rivera")
                .cardNumber("4444444444444444")
                .cardType("Visa")
                .expirationDate("10/28")
                .build();

        assertThrows(MaxPaymentCardsException.class, () ->
                userService.addPaymentCard("alex-123", fourthCard)
        );
    }

    @Test
    @DisplayName("TC8 & TC9: Add and retrieve favorite movies")
    void testTC8_TC9_FavoritesManagement() {
        User user = User.builder()
                .id("fav-user-1")
                .favoriteMovieIds(new ArrayList<>())
                .build();

        when(userRepository.findById("fav-user-1")).thenReturn(Optional.of(user));
        when(movieRepository.existsById("movie-dune-2")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.addFavoriteMovie("fav-user-1", "movie-dune-2");

        assertTrue(user.getFavoriteMovieIds().contains("movie-dune-2"));
    }
}
