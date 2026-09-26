package com.cinema.ebook.util;

import com.cinema.ebook.model.Movie;
import com.cinema.ebook.repository.MovieRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * DataSeeder
 *
 * Automatically seeds the MongoDB database with sample movies when the application starts.
 * Only seeds data if the database is empty (no movies exist), unless app.seed.reset=true.
 *
 * Includes:
 * - 5 CURRENTLY_RUNNING movies (with hardcoded showtimes and a weekly show-day schedule)
 * - 8 COMING_SOON movies
 * - All genres represented
 * - Real, embeddable YouTube trailer URLs and poster images
 *
 * @author Team 3 Backend
 * @version 1.1
 */
@Component
@Slf4j
public class DataSeeder {

    /**
     * Hardcoded showtimes for Sprint 1. Later sprints will derive these from showings.
     */
    private static final List<String> DEFAULT_SHOWTIMES = Arrays.asList("2:00 PM", "5:00 PM", "8:00 PM");

    /**
     * Weekly schedules for currently running movies. They differ per movie so the
     * show-date filter (Today / Tomorrow / This Weekend) returns different results.
     */
    private static final List<DayOfWeek> EVERY_DAY = List.of(DayOfWeek.values());

    /**
     * Seats per showing in the prototype auditorium (7 rows x 10 seats) times three showings
     */
    private static final int DEFAULT_AVAILABLE_SEATS = 70 * 3;

    private final MovieRepository movieRepository;

    @Value("${app.seed.reset:false}")
    private boolean resetOnStartup;

    /**
     * Constructor with dependency injection
     *
     * @param movieRepository the movie repository
     */
    public DataSeeder(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Seeds the database with sample movies on application startup
     * Triggered by ApplicationReadyEvent (after application fully starts)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        log.info("Starting database seeding...");

        if (resetOnStartup) {
            log.info("app.seed.reset=true - clearing existing movies");
            movieRepository.deleteAll();
        } else if (movieRepository.existsByStatusAndShowDaysIsNull("CURRENTLY_RUNNING")) {
            // Databases seeded before show-day schedules existed can't support the date filter
            log.info("Existing movies have no show-day schedule - re-seeding with the current sample data");
            movieRepository.deleteAll();
        }

        // Check if database is already populated
        long movieCount = movieRepository.count();

        if (movieCount > 0) {
            log.info("Database already contains {} movies. Skipping seeding.", movieCount);
            return;
        }

        log.info("Database is empty. Seeding with sample movies...");

        List<Movie> movies = createSampleMovies();
        movieRepository.saveAll(movies);

        log.info("Successfully seeded database with {} movies", movies.size());
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
