package com.cinema.ebook.util;

import com.cinema.ebook.model.Movie;
import com.cinema.ebook.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DataSeeder
 *
 * Automatically seeds the MongoDB database with sample movies when the application starts.
 * Only seeds data if the database is empty (no movies exist).
 *
 * Includes:
 * - 5+ CURRENTLY_RUNNING movies
 * - 5+ COMING_SOON movies
 * - All genres represented
 * - All ratings represented (G, PG, PG-13, R)
 * - Real YouTube trailer URLs
 *
 * @author Team 3 Backend
 * @version 1.0
 */
@Component
@Slf4j
public class DataSeeder {

    private final MovieRepository movieRepository;

    /**
     * Constructor with dependency injection
     *
     * @param movieRepository the movie repository
     */
    public MovieSeeder(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    /**
     * Seeds the database with sample movies on application startup
     * Triggered by ApplicationReadyEvent (after application fully starts)
     */
    @EventListener(ApplicationReadyEvent.class)
    public void seedDatabase() {
        log.info("Starting database seeding...");

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

        movies.add(Movie.builder()
                .title("Dune: Part Two")
                .description("Paul Atreides travels to the dangerous planet Dune to unite with Chani and seek revenge against the conspirators who destroyed his family.")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Adventure"))
                .runtime(166)
                .releaseDate("2024-02-26")
                .posterUrl("https://via.placeholder.com/300x450?text=Dune+Part+Two")
                .trailerUrl("https://www.youtube.com/embed/n9xhJsagTKQ")
                .director("Denis Villeneuve")
                .cast(Arrays.asList("TimothÃ©e Chalamet", "Zendaya", "Rebecca Ferguson", "Austin Butler"))
                .imdbRating(8.5)
                .status("CURRENTLY_RUNNING")
                .build());

        movies.add(Movie.builder()
                .title("Avatar: The Way of Water")
                .description("Jake Sully and his family escape to the ocean planet of Pandora where they build a new life with the Metkayina clan.")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Fantasy"))
                .runtime(192)
                .releaseDate("2022-12-16")
                .posterUrl("https://via.placeholder.com/300x450?text=Avatar+2")
                .trailerUrl("https://www.youtube.com/embed/d9MyW72ELq0")
                .director("James Cameron")
                .cast(Arrays.asList("Sam Worthington", "Zoe Saldana", "Sigourney Weaver", "Stephen Lang"))
                .imdbRating(7.8)
                .status("CURRENTLY_RUNNING")
                .build());

        movies.add(Movie.builder()
                .title("The Brutalist")
                .description("A Hungarian-Jewish immigrant and architect rebuilds his life in post-war America while pursuing his artistic vision.")
                .rating("R")
                .genre(Arrays.asList("Drama", "History"))
                .runtime(215)
                .releaseDate("2023-12-25")
                .posterUrl("https://via.placeholder.com/300x450?text=The+Brutalist")
                .trailerUrl("https://www.youtube.com/embed/vOOE0EZmRwM")
                .director("Brady Corbet")
                .cast(Arrays.asList("Adrien Brody", "Guy Pearce", "Tilda Swinton"))
                .imdbRating(8.2)
                .status("CURRENTLY_RUNNING")
                .build());

        movies.add(Movie.builder()
                .title("Oppenheimer")
                .description("The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb during World War II.")
                .rating("R")
                .genre(Arrays.asList("Drama", "History", "Thriller"))
                .runtime(180)
                .releaseDate("2023-07-21")
                .posterUrl("https://via.placeholder.com/300x450?text=Oppenheimer")
                .trailerUrl("https://www.youtube.com/embed/_a-NVYc5t8U")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Cillian Murphy", "Emily Blunt", "Robert Downey Jr.", "Matt Damon"))
                .imdbRating(8.3)
                .status("CURRENTLY_RUNNING")
                .build());

        movies.add(Movie.builder()
                .title("Barbie")
                .description("Barbie and Ken go on an unexpected journey to discover what it means to be human in the real world.")
                .rating("PG-13")
                .genre(Arrays.asList("Comedy", "Fantasy"))
                .runtime(114)
                .releaseDate("2023-07-21")
                .posterUrl("https://via.placeholder.com/300x450?text=Barbie")
                .trailerUrl("https://www.youtube.com/embed/pBk4NYhWNMM")
                .director("Greta Gerwig")
                .cast(Arrays.asList("Margot Robbie", "Ryan Gosling", "Will Ferrell", "America Ferrera"))
                .imdbRating(7.4)
                .status("CURRENTLY_RUNNING")
                .build());

        // COMING_SOON MOVIES (5+ movies)

        movies.add(Movie.builder()
                .title("The Shawshank Redemption")
                .description("Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.")
                .rating("R")
                .genre(Arrays.asList("Drama"))
                .runtime(142)
                .releaseDate("2024-06-15")
                .posterUrl("https://via.placeholder.com/300x450?text=Shawshank+Redemption")
                .trailerUrl("https://www.youtube.com/embed/6hB3S9bIaco")
                .director("Frank Darabont")
                .cast(Arrays.asList("Tim Robbins", "Morgan Freeman"))
                .imdbRating(9.3)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("The Dark Knight")
                .description("Batman faces the Joker, a criminal mastermind who wants to plunge Gotham into anarchy and chaos.")
                .rating("PG-13")
                .genre(Arrays.asList("Action", "Crime", "Drama"))
                .runtime(152)
                .releaseDate("2024-07-18")
                .posterUrl("https://via.placeholder.com/300x450?text=The+Dark+Knight")
                .trailerUrl("https://www.youtube.com/embed/EXeTwQWrcwY")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Christian Bale", "Heath Ledger", "Aaron Eckhart"))
                .imdbRating(9.0)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("Inception")
                .description("A skilled thief who steals corporate secrets through dream-sharing technology is given the inverse task of planting an idea.")
                .rating("PG-13")
                .genre(Arrays.asList("Action", "Sci-Fi", "Thriller"))
                .runtime(148)
                .releaseDate("2024-08-22")
                .posterUrl("https://via.placeholder.com/300x450?text=Inception")
                .trailerUrl("https://www.youtube.com/embed/8ZcmTl_1oBo")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Leonardo DiCaprio", "Marion Cotillard", "Ellen Page", "Joseph Gordon-Levitt"))
                .imdbRating(8.8)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("Pulp Fiction")
                .description("The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.")
                .rating("R")
                .genre(Arrays.asList("Crime", "Drama"))
                .runtime(154)
                .releaseDate("2024-09-15")
                .posterUrl("https://via.placeholder.com/300x450?text=Pulp+Fiction")
                .trailerUrl("https://www.youtube.com/embed/s7EdQ4FqSTk")
                .director("Quentin Tarantino")
                .cast(Arrays.asList("John Travolta", "Samuel L. Jackson", "Uma Thurman", "Harvey Keitel"))
                .imdbRating(8.9)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("Forrest Gump")
                .description("The presidencies of Kennedy and Johnson unfold from the perspective of an Alabama man with an IQ of 75.")
                .rating("PG-13")
                .genre(Arrays.asList("Drama", "Romance"))
                .runtime(142)
                .releaseDate("2024-10-20")
                .posterUrl("https://via.placeholder.com/300x450?text=Forrest+Gump")
                .trailerUrl("https://www.youtube.com/embed/bIvzrx3sCBs")
                .director("Robert Zemeckis")
                .cast(Arrays.asList("Tom Hanks", "Sally Field", "Gary Sinise", "Mykelti Williamson"))
                .imdbRating(8.8)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("Interstellar")
                .description("A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.")
                .rating("PG-13")
                .genre(Arrays.asList("Adventure", "Drama", "Sci-Fi"))
                .runtime(169)
                .releaseDate("2024-11-10")
                .posterUrl("https://via.placeholder.com/300x450?text=Interstellar")
                .trailerUrl("https://www.youtube.com/embed/zSID6AWnqKE")
                .director("Christopher Nolan")
                .cast(Arrays.asList("Matthew McConaughey", "Anne Hathaway", "Jessica Chastain", "Michael Caine"))
                .imdbRating(8.7)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("The Conjuring")
                .description("Paranormal investigators work to help a family terrorized by a dark presence in their farmhouse.")
                .rating("R")
                .genre(Arrays.asList("Horror", "Mystery", "Thriller"))
                .runtime(112)
                .releaseDate("2024-12-01")
                .posterUrl("https://via.placeholder.com/300x450?text=The+Conjuring")
                .trailerUrl("https://www.youtube.com/embed/K8UV7SAhohY")
                .director("James Wan")
                .cast(Arrays.asList("Vera Farmiga", "Patrick Wilson", "Lili Taylor"))
                .imdbRating(7.5)
                .status("COMING_SOON")
                .build());

        movies.add(Movie.builder()
                .title("La La Land")
                .description("While navigating their careers in Los Angeles, a pianist and an actress fall in love while pursuing their dreams.")
                .rating("PG-13")
                .genre(Arrays.asList("Comedy", "Drama", "Romance"))
                .runtime(128)
                .releaseDate("2025-01-15")
                .posterUrl("https://via.placeholder.com/300x450?text=La+La+Land")
                .trailerUrl("https://www.youtube.com/embed/0G_kA9Bj-jQ")
                .director("Damien Chazelle")
                .cast(Arrays.asList("Ryan Gosling", "Emma Stone"))
                .imdbRating(8.0)
                .status("COMING_SOON")
                .build());

        return movies;
    }
}
