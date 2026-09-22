package com.cinema.ebook.service;

import com.cinema.ebook.model.Movie;
import com.cinema.ebook.dto.MovieDTO;

import java.util.List;
import java.util.Map;

/**
 * MovieService Interface
 *
 * Defines the contract for movie-related business logic.
 * Handles search, filtering, and data transformation.
 *
 *
 * @author Team 3 Backend
 * @version 2.0
 */
public interface MovieService {

    // ==================== RETRIEVAL METHODS ====================

    /**
     * Get all movies, optionally filtered by status
     *
     * FEEDBACK FIX: Returns movies with available showtimes when status=CURRENTLY_RUNNING
     *
     * @param status optional status filter (CURRENTLY_RUNNING or COMING_SOON)
     * @return List of MovieDTOs matching criteria
     * @throws com.cinema.ebook.exception.InvalidFilterException if status is invalid
     */
    List<MovieDTO> getAllMovies(String status);

    /**
     * Get a single movie by ID
     *
     * @param id the movie ID
     * @return the Movie entity or null if not found
     */
    Movie getMovieById(String id);

    /**
     * Get all available genres for the genre filter dropdown
     *
     * @return List of all genre names
     */
    List<String> getAllGenres();

    // ==================== SEARCH METHODS ====================

    /**
     * Search movies by title (case-insensitive, partial match)
     *
     * @param title the title to search for
     * @return Map containing: movies (List), total (count), query (search term)
     */
    Map<String, Object> searchByTitle(String title);

    // ==================== FILTER METHODS ====================

    /**
     * Filter movies by genre only
     *
     * @param genre the genre to filter by
     * @return Map with movies, total, appliedFilters
     * @throws com.cinema.ebook.exception.InvalidFilterException if genre is invalid
     */
    Map<String, Object> filterByGenre(String genre);

    /**
     * Filter movies by rating only
     *
     * @param rating the age rating to filter by (G, PG, PG-13, R)
     * @return Map with movies, total, appliedFilters
     * @throws com.cinema.ebook.exception.InvalidFilterException if rating is invalid
     */
    Map<String, Object> filterByRating(String rating);

    /**
     * Filter movies by status only
     *
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return Map with movies, total, appliedFilters
     * @throws com.cinema.ebook.exception.InvalidFilterException if status is invalid
     */
    Map<String, Object> filterByStatus(String status);

    /**
     * Filter movies by genre and rating
     *
     * @param genre the genre to filter by
     * @param rating the age rating to filter by
     * @return Map with movies, total, appliedFilters
     */
    Map<String, Object> filterByGenreAndRating(String genre, String rating);

    /**
     * Filter movies by genre and status
     *
     * @param genre the genre to filter by
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return Map with movies, total, appliedFilters
     */
    Map<String, Object> filterByGenreAndStatus(String genre, String status);

    /**
     * Filter movies by rating and status
     *
     * @param rating the age rating to filter by
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return Map with movies, total, appliedFilters
     */
    Map<String, Object> filterByRatingAndStatus(String rating, String status);

    /**
     * Filter movies by all three criteria: genre, rating, and status
     *
     * @param genre the genre to filter by
     * @param rating the age rating to filter by
     * @param status CURRENTLY_RUNNING or COMING_SOON
     * @return Map with movies, total, appliedFilters
     */
    Map<String, Object> filterByAllCriteria(String genre, String rating, String status);

    // ==================== VALIDATION METHODS ====================

    /**
     * Check if a genre is valid
     *
     * @param genre the genre to validate
     * @return true if valid
     */
    boolean isValidGenre(String genre);

    /**
     * Check if a rating is valid
     *
     * @param rating the rating to validate
     * @return true if valid
     */
    boolean isValidRating(String rating);

    /**
     * Check if a status is valid
     *
     * @param status the status to validate
     * @return true if valid
     */
    boolean isValidStatus(String status);

    /**
     * Check if a movie exists
     *
     * @param title the movie title to check
     * @return true if movie exists
     */
    boolean movieExists(String title);

    // ==================== UTILITY METHODS ====================

    /**
     * Count movies by status
     * Used for statistics and monitoring
     *
     * @param status the status to count
     * @return number of movies with this status
     */
    long countByStatus(String status);
}
