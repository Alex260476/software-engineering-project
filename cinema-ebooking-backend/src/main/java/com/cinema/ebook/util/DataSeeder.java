package com.cinema.ebook.util;

import com.cinema.ebook.model.Address;
import com.cinema.ebook.model.Movie;
import com.cinema.ebook.model.PaymentCard;
import com.cinema.ebook.model.User;
import com.cinema.ebook.model.enums.UserStatus;
import com.cinema.ebook.model.enums.UserType;
import com.cinema.ebook.repository.MovieRepository;
import com.cinema.ebook.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DataSeeder
 *
 * Automatically seeds the MongoDB database with sample movies and Sprint 2 demo accounts
 * when the application starts.
 */
@Component
@Slf4j
public class DataSeeder {

    private static final List<String> DEFAULT_SHOWTIMES = Arrays.asList("2:00 PM", "5:00 PM", "8:00 PM");
    private static final List<DayOfWeek> EVERY_DAY = List.of(DayOfWeek.values());
    private static final int DEFAULT_AVAILABLE_SEATS = 70 * 3;

    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final PasswordHashingUtil passwordHashingUtil;
    private final EncryptionUtil encryptionUtil;

    @Value("${app.seed.reset:false}")
    private boolean resetOnStartup;

    public DataSeeder(MovieRepository movieRepository,
                      UserRepository userRepository,
                      PasswordHashingUtil passwordHashingUtil,
                      EncryptionUtil encryptionUtil) {
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.passwordHashingUtil = passwordHashingUtil;
        this.encryptionUtil = encryptionUtil;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        log.info("Starting database seeding...");

        if (resetOnStartup) {
            log.info("app.seed.reset=true - clearing existing movies and users");
            movieRepository.deleteAll();
            userRepository.deleteAll();
        } else if (movieRepository.existsByStatusAndShowDaysIsNull("CURRENTLY_RUNNING")) {
            log.info("Existing movies have no show-day schedule - re-seeding with the current sample data");
            movieRepository.deleteAll();
        }

        // Check if movies exist
        long movieCount = movieRepository.count();
        if (movieCount == 0) {
            log.info("Database has no movies. Seeding sample movies...");
            List<Movie> movies = createSampleMovies();
            movieRepository.saveAll(movies);
            log.info("Successfully seeded database with {} movies", movies.size());
        } else {
            log.info("Database already contains {} movies.", movieCount);
        }

        // Seed Sprint 2 Demo Users
        seedUsers();
    }

    /**
     * Seeds the required Sprint 2 demo accounts:
     * 1. Admin account (admin@cinema.com / AdminPass!20)
     * 2. Verified user with favorite movies (favuser@gmail.com / UserPass!20)
     * 3. Verified user with 3 payment cards (cardsuser@gmail.com / UserPass!20)
     */
    public void seedUsers() {
        log.info("Checking/seeding Sprint 2 demo accounts...");

        // 1. Admin Account
        if (userRepository.findByEmail("admin@cinema.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Admin")
                    .email("admin@cinema.com")
                    .passwordHash(passwordHashingUtil.hashPassword("AdminPass!20"))
                    .userType(UserType.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .promotionSubscription(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
            log.info("Seeded Admin account: admin@cinema.com / AdminPass!20");
        }

        // Fetch movies for favorites
        List<Movie> allMovies = movieRepository.findAll();
        List<String> favMovieIds = allMovies.stream()
                .limit(2)
                .map(Movie::getId)
                .collect(Collectors.toList());

        // 2. Verified user with at least one favorite movie
        if (userRepository.findByEmail("favuser@gmail.com").isEmpty()) {
            Address address = Address.builder()
                    .street("123 Peach Street")
                    .city("Atlanta")
                    .state("GA")
                    .zipCode("30303")
                    .build();

            PaymentCard card = PaymentCard.builder()
                    .id(UUID.randomUUID().toString())
                    .cardholderName("Sarah Jenkins")
                    .cardType("Visa")
                    .cardNumberEncrypted(encryptionUtil.encrypt("4242424242424242"))
                    .cardLastFour("4242")
                    .expirationDate("11/27")
                    .billingAddress(address)
                    .addedAt(LocalDateTime.now())
                    .build();

            User favUser = User.builder()
                    .firstName("Sarah")
                    .lastName("Jenkins")
                    .email("favuser@gmail.com")
                    .passwordHash(passwordHashingUtil.hashPassword("UserPass!20"))
                    .userType(UserType.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .promotionSubscription(true)
                    .address(address)
                    .paymentCards(new ArrayList<>(List.of(card)))
                    .favoriteMovieIds(favMovieIds) // Has favorite movies stored!
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(favUser);
            log.info("Seeded User with favorites: favuser@gmail.com / UserPass!20 (favorites: {})", favMovieIds.size());
        }

        // 3. Verified user with 3 payment cards (TC7 test data)
        if (userRepository.findByEmail("cardsuser@gmail.com").isEmpty()) {
            Address address = Address.builder()
                    .street("456 Elm Ave")
                    .city("Athens")
                    .state("GA")
                    .zipCode("30602")
                    .build();

            PaymentCard card1 = PaymentCard.builder()
                    .id(UUID.randomUUID().toString())
                    .cardholderName("Alex Rivera")
                    .cardType("Visa")
                    .cardNumberEncrypted(encryptionUtil.encrypt("4111111111111111"))
                    .cardLastFour("1111")
                    .expirationDate("12/28")
                    .billingAddress(address)
                    .addedAt(LocalDateTime.now())
                    .build();

            PaymentCard card2 = PaymentCard.builder()
                    .id(UUID.randomUUID().toString())
                    .cardholderName("Alex Rivera")
                    .cardType("MasterCard")
                    .cardNumberEncrypted(encryptionUtil.encrypt("5500000000002222"))
                    .cardLastFour("2222")
                    .expirationDate("08/27")
                    .billingAddress(address)
                    .addedAt(LocalDateTime.now())
                    .build();

            PaymentCard card3 = PaymentCard.builder()
                    .id(UUID.randomUUID().toString())
                    .cardholderName("Alex Rivera")
                    .cardType("Amex")
                    .cardNumberEncrypted(encryptionUtil.encrypt("378282246313333"))
                    .cardLastFour("3333")
                    .expirationDate("05/29")
                    .billingAddress(address)
                    .addedAt(LocalDateTime.now())
                    .build();

            User cardsUser = User.builder()
                    .firstName("Alex")
                    .lastName("Rivera")
                    .email("cardsuser@gmail.com")
                    .passwordHash(passwordHashingUtil.hashPassword("UserPass!20"))
                    .userType(UserType.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .emailVerified(true)
                    .promotionSubscription(true)
                    .address(address)
                    .paymentCards(new ArrayList<>(List.of(card1, card2, card3))) // Exactly 3 payment cards!
                    .favoriteMovieIds(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(cardsUser);
            log.info("Seeded User with 3 payment cards: cardsuser@gmail.com / UserPass!20");
        }
    }

    /**
     * Create all sample movies for the database
     *
     * @return List of Movie objects
     */
    private List<Movie> createSampleMovies() {
        List<Movie> movies = new ArrayList<>();

        // CURRENTLY_RUNNING MOVIES (5 movies)

        movies.add(nowPlaying(Movie.builder()
                .title("Dune: Part Two")
                .description("Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family, and must choose between the love of his life and the fate of the universe.")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Adventure"))
                .runtime(166)
                .releaseDate("2024-03-01")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/5/52/Dune_Part_Two_poster.jpeg")
                .trailerUrl("https://www.youtube.com/embed/Way9Dexny3w")
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Timothée Chalamet", "Zendaya", "Rebecca Ferguson", "Austin Butler"))
                .imdbRating(8.5), EVERY_DAY));

        movies.add(nowPlaying(Movie.builder()
                .title("Avatar: The Way of Water")
                .description("Jake Sully and his family are forced to leave their home and take refuge with the ocean-dwelling Metkayina clan of Pandora.")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Fantasy"))
                .runtime(192)
                .releaseDate("2022-12-16")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/5/54/Avatar_The_Way_of_Water_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/d9MyW72ELq0")
                .director("James Cameron")
                .cast(Arrays.asList("Sam Worthington", "Zoe Saldaña", "Sigourney Weaver", "Stephen Lang"))
                .imdbRating(7.6), List.of(FRIDAY, SATURDAY, SUNDAY)));

        movies.add(nowPlaying(Movie.builder()
                .title("The Brutalist")
                .description("A Hungarian-Jewish immigrant and architect rebuilds his life in post-war America while pursuing his artistic vision.")
                .rating("R")
                .genre(Arrays.asList("Drama", "History"))
                .runtime(215)
                .releaseDate("2024-12-20")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/7/7c/TheBrutalist2024.png")
                .trailerUrl("https://www.youtube.com/embed/GdRXPAHIEW4")
                .director("Brady Corbet")
                .cast(Arrays.asList("Adrien Brody", "Felicity Jones", "Guy Pearce"))
                .imdbRating(7.4), List.of(SATURDAY, SUNDAY)));

        movies.add(nowPlaying(Movie.builder()
                .title("Oppenheimer")
                .description("The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.")
                .rating("R")
                .genre(Arrays.asList("Drama", "History", "Thriller"))
                .runtime(180)
                .releaseDate("2023-07-21")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/4/4a/Oppenheimer_%28film%29.jpg")
                .trailerUrl("https://www.youtube.com/embed/uYPbbksJxIg")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Cillian Murphy", "Emily Blunt", "Robert Downey Jr.", "Matt Damon"))
                .imdbRating(8.3), List.of(MONDAY, TUESDAY, WEDNESDAY, THURSDAY)));

        movies.add(nowPlaying(Movie.builder()
                .title("Barbie")
                .description("Barbie and Ken go on an unexpected journey to discover what it means to be human in the real world.")
                .rating("PG-13")
                .genre(Arrays.asList("Comedy", "Fantasy"))
                .runtime(114)
                .releaseDate("2023-07-21")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/0/0b/Barbie_2023_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/pBk4NYhWNMM")
                .director("Greta Gerwig")
                .cast(Arrays.asList("Margot Robbie", "Ryan Gosling", "Will Ferrell", "America Ferrera"))
                .imdbRating(6.8), List.of(TUESDAY, THURSDAY, SATURDAY, SUNDAY)));

        // COMING_SOON MOVIES (8 movies - anniversary re-releases)

        movies.add(comingSoon(Movie.builder()
                .title("The Shawshank Redemption")
                .description("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.")
                .rating("R")
                .genre(Arrays.asList("Drama"))
                .runtime(142)
                .releaseDate("2026-10-16")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/8/81/ShawshankRedemptionMoviePoster.jpg")
                .trailerUrl("https://www.youtube.com/embed/6hB3S9bIaco")
                .director("Frank Darabont")
                .cast(Arrays.asList("Tim Robbins", "Morgan Freeman"))
                .imdbRating(9.3)));

        movies.add(comingSoon(Movie.builder()
                .title("The Dark Knight")
                .description("Batman faces the Joker, a criminal mastermind who wants to plunge Gotham into anarchy and chaos.")
                .rating("PG-13")
                .genre(Arrays.asList("Action", "Crime", "Drama"))
                .runtime(152)
                .releaseDate("2026-10-30")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/1/1c/The_Dark_Knight_%282008_film%29.jpg")
                .trailerUrl("https://www.youtube.com/embed/EXeTwQWrcwY")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Christian Bale", "Heath Ledger", "Aaron Eckhart"))
                .imdbRating(9.0)));

        movies.add(comingSoon(Movie.builder()
                .title("Inception")
                .description("A skilled thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.")
                .rating("PG-13")
                .genre(Arrays.asList("Action", "Sci-Fi", "Thriller"))
                .runtime(148)
                .releaseDate("2026-11-13")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/2/2e/Inception_%282010%29_theatrical_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/YoHD9XEInc0")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Leonardo DiCaprio", "Marion Cotillard", "Elliot Page", "Joseph Gordon-Levitt"))
                .imdbRating(8.8)));

        movies.add(comingSoon(Movie.builder()
                .title("Pulp Fiction")
                .description("The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.")
                .rating("R")
                .genre(Arrays.asList("Crime", "Drama"))
                .runtime(154)
                .releaseDate("2026-11-27")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/3/3b/Pulp_Fiction_%281994%29_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/tGpTpVyI_OQ")
                .director("Quentin Tarantino")
                .cast(Arrays.asList("John Travolta", "Samuel L. Jackson", "Uma Thurman", "Harvey Keitel"))
                .imdbRating(8.9)));

        movies.add(comingSoon(Movie.builder()
                .title("Forrest Gump")
                .description("The presidencies of Kennedy and Johnson, the Vietnam War, and other historical events unfold from the perspective of an Alabama man with an IQ of 75.")
                .rating("PG-13")
                .genre(Arrays.asList("Drama", "Romance"))
                .runtime(142)
                .releaseDate("2026-12-11")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/6/67/Forrest_Gump_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/bLvqoHBptjg")
                .director("Robert Zemeckis")
                .cast(Arrays.asList("Tom Hanks", "Sally Field", "Gary Sinise", "Mykelti Williamson"))
                .imdbRating(8.8)));

        movies.add(comingSoon(Movie.builder()
                .title("Interstellar")
                .description("A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.")
                .rating("PG-13")
                .genre(Arrays.asList("Adventure", "Drama", "Sci-Fi"))
                .runtime(169)
                .releaseDate("2027-01-08")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/b/bc/Interstellar_film_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/zSWdZVtXT7E")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"))
                .imdbRating(8.7)));

        movies.add(comingSoon(Movie.builder()
                .title("The Conjuring")
                .description("Paranormal investigators work to help a family terrorized by a dark presence in their farmhouse.")
                .rating("R")
                .genre(Arrays.asList("Horror", "Mystery", "Thriller"))
                .runtime(112)
                .releaseDate("2027-01-22")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/8/8c/The_Conjuring_poster.jpg")
                .trailerUrl("https://www.youtube.com/embed/k10ETZ41q5o")
                .director("James Wan")
                .cast(Arrays.asList("Vera Farmiga", "Patrick Wilson", "Lili Taylor"))
                .imdbRating(7.5)));

        movies.add(comingSoon(Movie.builder()
                .title("La La Land")
                .description("While navigating their careers in Los Angeles, a pianist and an actress fall in love while pursuing their dreams.")
                .rating("PG-13")
                .genre(Arrays.asList("Comedy", "Drama", "Romance"))
                .runtime(128)
                .releaseDate("2027-02-12")
                .posterUrl("https://upload.wikimedia.org/wikipedia/en/a/ab/La_La_Land_%28film%29.png")
                .trailerUrl("https://www.youtube.com/embed/0pdqf4P9MB8")
                .director("Damien Chazelle")
                .cast(Arrays.asList("Ryan Gosling", "Emma Stone"))
                .imdbRating(8.0)));

        return movies;
    }

    private Movie nowPlaying(Movie.MovieBuilder builder, List<DayOfWeek> showDays) {
        return builder
                .status("CURRENTLY_RUNNING")
                .availableShowtimes(DEFAULT_SHOWTIMES)
                .showDays(showDays.stream().map(DayOfWeek::name).toList())
                .totalAvailableSeats(DEFAULT_AVAILABLE_SEATS)
                .build();
    }

    private Movie comingSoon(Movie.MovieBuilder builder) {
        return builder
                .status("COMING_SOON")
                .availableShowtimes(Collections.emptyList())
                .showDays(Collections.emptyList())
                .totalAvailableSeats(0)
                .build();
    }
}
