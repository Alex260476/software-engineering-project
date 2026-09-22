package com.cinema.ebook.repository;

import com.cinema.ebook.model.Movie;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MovieRepository
 *
 * Spring Data MongoDB repository for Movie entity.
 * Provides specialized query methods for searching and filtering movies.
 *
 *
 * @author Team 3 Backend
 * @version 2.0 (Updated for feedback)
 */
@Repository
public interface MovieRepository extends MongoRepository<Movie, String> {

    // ==================== STATUS QUERIES ====================

    /**
     * Find all movies by status (Now Playing or Coming Soon)
     *
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return List of movies with matching status
     */
    List<Movie> findByStatus(String status);

    /**
     * Count movies by status
     *
     * @param status the status to count
     * @return count of movies with this status
     */
    long countByStatus(String status);

    // ==================== SEARCH QUERIES ====================

    /**
     * Find movies by title (case-insensitive, partial match)
     *
     * @param title the title substring to search
     * @return List of movies with matching titles
     */
    List<Movie> findByTitleIgnoreCaseContaining(String title);

    /**
     * Check if a movie exists by title (case-insensitive)
     *
     * @param title the exact title to check
     * @return true if movie exists
     */
    boolean existsByTitleIgnoreCase(String title);

    /**
     * Find a movie by exact title (case-insensitive)
     *
     * @param title the exact title
     * @return Optional containing the movie if found
     */
    Optional<Movie> findByTitleIgnoreCase(String title);

    // ==================== GENRE QUERIES ====================

    /**
     * Find movies by genre (supports movies with multiple genres)
     *
     * @param genre the genre to search for
     * @return List of movies containing this genre
     */
    List<Movie> findByGenreContaining(String genre);

    /**
     * Count movies by genre
     *
     * @param genre the genre to count
     * @return number of movies with this genre
     */
    long countByGenreContaining(String genre);

    // ==================== RATING QUERIES ====================

    /**
     * Find movies by age rating
     *
     * @param rating G, PG, PG-13, or R
     * @return List of movies with matching rating
     */
    List<Movie> findByRating(String rating);

    /**
     * Count movies by rating
     *
     * @param rating the rating to count
     * @return number of movies with this rating
     */
    long countByRating(String rating);

    // ==================== MULTI-FILTER QUERIES ====================

    /**
     * Find movies by genre and status
     *
     * @param genre the genre to filter by
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return List of movies matching both criteria
     */
    List<Movie> findByGenreContainingAndStatus(String genre, String status);

    /**
     * Find movies by genre and rating
     *
     * @param genre the genre to filter by
     * @param rating G, PG, PG-13, or R
     * @return List of movies matching both criteria
     */
    List<Movie> findByGenreContainingAndRating(String genre, String rating);

    /**
     * Find movies by rating and status
     *
     * @param rating G, PG, PG-13, or R
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return List of movies matching both criteria
     */
    List<Movie> findByRatingAndStatus(String rating, String status);

    /**
     * Find movies by all three criteria: genre, rating, and status
     *
     * @param genre the genre to filter by
     * @param rating G, PG, PG-13, or R
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return List of movies matching all three criteria
     */
    List<Movie> findByGenreContainingAndRatingAndStatus(String genre, String rating, String status);

    // ==================== AVAILABILITY QUERIES ====================

    /**
     * Find all Currently Running movies with available showtimes
     * Used for home page "Now Playing" section
     *
     *
     * @return List of movies currently playing with available showtimes
     */
    @Query("{ 'status': 'CURRENTLY_RUNNING', 'availableShowtimes': { $exists: true, $ne: [] } }")
    List<Movie> findNowPlayingWithShowtimes();

    /**
     * Find all Coming Soon movies
     * Used for home page "Coming Soon" section
     *
     * @return List of movies coming soon
     */
    List<Movie> findByStatusOrderByReleaseDateAsc(String status);

    /**
     * Find movies with available seats
     * Used to filter out sold-out movies
     *
     * @return List of movies with available seats
     */
    @Query("{ 'totalAvailableSeats': { $gt: 0 } }")
    List<Movie> findMoviesWithAvailableSeats();

    // ==================== SORTING QUERIES ====================

    /**
     * Find movies by status, ordered by release date
     *
     * @param status the status to filter by
     * @return List of movies ordered by release date (newest first)
     */
    List<Movie> findByStatusOrderByReleaseDateDesc(String status);

    /**
     * Find all movies ordered by IMDb rating
     *
     * @return List of movies ordered by rating (highest first)
     */
    List<Movie> findAllByOrderByImdbRatingDesc();

    // ==================== HELPER QUERIES ====================

    /**
     * Find a movie by ID
     *
     * @param id the MongoDB ObjectId as String
     * @return Optional containing the movie if found
     */
    Optional<Movie> findById(String id);

    /**
     * Check if a movie exists
     *
     * @param id the movie ID
     * @return true if movie exists
     */
    boolean existsById(String id);

    /**
     * Get total count of movies
     *
     * @return total number of movies in database
     */
    long count();

    /**
     * Find recently added movies (for admin dashboard)
     *
     * @return List of movies ordered by creation date (newest first)
     */
    List<Movie> findAllByOrderByCreatedAtDesc();
}
