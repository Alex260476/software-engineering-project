package com.cinema.ebook.service;

import com.cinema.ebook.model.Movie;
import com.cinema.ebook.model.enums.AgeRating;
import com.cinema.ebook.model.enums.Genre;
import com.cinema.ebook.model.enums.MovieStatus;
import com.cinema.ebook.dto.MovieDTO;
import com.cinema.ebook.exception.InvalidFilterException;
import com.cinema.ebook.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MovieServiceImpl
 *
 * Implementation of MovieService interface.
 * Handles all business logic for movie operations: search, filter, validation.
 * Converts Movie entities to MovieDTOs for API responses.
 *
 *
 * @author Team 3 Backend
 * @version 2.0
 */
@Service
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    /**
     * Constructor with dependency injection
     *
     * @param movieRepository the movie repository
     */
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // ==================== RETRIEVAL METHODS ====================

    @Override
    public List<MovieDTO> getAllMovies(String status) {
        log.info("Fetching all movies with status filter: {}", status);

        List<Movie> movies;
        if (status == null || status.isEmpty()) {
            movies = movieRepository.findAll();
            log.info("Fetched {} movies without status filter", movies.size());
        } else {
            if (!isValidStatus(status)) {
                String validValues = String.join(", ", MovieStatus.getAllValues());
                throw InvalidFilterException.forField("status", status, validValues);
            }
            movies = movieRepository.findByStatus(status);
            log.info("Fetched {} movies with status: {}", movies.size(), status);
        }

        return movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Movie getMovieById(String id) {
        log.info("Fetching movie by ID: {}", id);
        Optional<Movie> movie = movieRepository.findById(id);
        return movie.orElse(null);
    }

    @Override
    public List<String> getAllGenres() {
        log.info("Fetching all available genres");
        return Genre.getAllValues();
    }

    // ==================== SEARCH METHODS ====================

    @Override
    public Map<String, Object> searchByTitle(String title) {
        log.info("Searching for movies with title: {}", title);

        Map<String, Object> result = new HashMap<>();

        if (title == null || title.isEmpty()) {
            result.put("movies", new ArrayList<>());
            result.put("total", 0);
            result.put("query", title);
            return result;
        }

        String cleanTitle = title.trim();
        List<Movie> movies = movieRepository.findByTitleIgnoreCaseContaining(cleanTitle);
        List<MovieDTO> dtos = movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        result.put("movies", dtos);
        result.put("total", dtos.size());
        result.put("query", cleanTitle);

        log.info("Found {} movies matching title: {}", dtos.size(), cleanTitle);
        return result;
    }

    // ==================== FILTER METHODS ====================

    @Override
    public Map<String, Object> filterByGenre(String genre) {
        log.info("Filtering by genre: {}", genre);
        validateGenre(genre);

        List<Movie> movies = movieRepository.findByGenreContaining(genre);
        return buildFilterResponse(movies, genre, null, null);
    }

    @Override
    public Map<String, Object> filterByRating(String rating) {
        log.info("Filtering by rating: {}", rating);
        validateRating(rating);

        List<Movie> movies = movieRepository.findByRating(rating);
        return buildFilterResponse(movies, null, rating, null);
    }

    @Override
    public Map<String, Object> filterByStatus(String status) {
        log.info("Filtering by status: {}", status);
        validateStatus(status);

        List<Movie> movies = movieRepository.findByStatus(status);
        return buildFilterResponse(movies, null, null, status);
    }

    @Override
    public Map<String, Object> filterByGenreAndRating(String genre, String rating) {
        log.info("Filtering by genre: {} and rating: {}", genre, rating);
        validateGenre(genre);
        validateRating(rating);

        List<Movie> movies = movieRepository.findByGenreContainingAndRating(genre, rating);
        return buildFilterResponse(movies, genre, rating, null);
    }

    @Override
    public Map<String, Object> filterByGenreAndStatus(String genre, String status) {
        log.info("Filtering by genre: {} and status: {}", genre, status);
        validateGenre(genre);
        validateStatus(status);

        List<Movie> movies = movieRepository.findByGenreContainingAndStatus(genre, status);
        return buildFilterResponse(movies, genre, null, status);
    }

    @Override
    public Map<String, Object> filterByRatingAndStatus(String rating, String status) {
        log.info("Filtering by rating: {} and status: {}", rating, status);
        validateRating(rating);
        validateStatus(status);

        List<Movie> movies = movieRepository.findByRatingAndStatus(rating, status);
        return buildFilterResponse(movies, null, rating, status);
    }

    @Override
    public Map<String, Object> filterByAllCriteria(String genre, String rating, String status) {
        log.info("Filtering by genre: {}, rating: {}, and status: {}", genre, rating, status);
        validateGenre(genre);
        validateRating(rating);
        validateStatus(status);

        List<Movie> movies = movieRepository.findByGenreContainingAndRatingAndStatus(genre, rating, status);
        return buildFilterResponse(movies, genre, rating, status);
    }

    // ==================== VALIDATION METHODS ====================

    @Override
    public boolean isValidGenre(String genre) {
        return genre != null && Genre.isValid(genre);
    }

    @Override
    public boolean isValidRating(String rating) {
        return rating != null && AgeRating.isValid(rating);
    }

    @Override
    public boolean isValidStatus(String status) {
        return status != null && MovieStatus.isValid(status);
    }

    @Override
    public boolean movieExists(String title) {
        return movieRepository.existsByTitleIgnoreCase(title);
    }

    @Override
    public long countByStatus(String status) {
        return movieRepository.countByStatus(status);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Convert Movie entity to MovieDTO for API response
     *
     * @param movie the movie entity
     * @return MovieDTO with all relevant fields
     */
    private MovieDTO convertToDTO(Movie movie) {
        return MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .rating(movie.getRating())
                .genre(movie.getGenre())
                .status(movie.getStatus())
                .description(movie.getDescription())
                .trailerUrl(movie.getTrailerUrl())
                .runtime(movie.getRuntime())
                .releaseDate(movie.getReleaseDate())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .imdbRating(movie.getImdbRating())
                .availableShowtimes(movie.getAvailableShowtimes())
                .totalAvailableSeats(movie.getTotalAvailableSeats())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .build();
    }

    /**
     * Build consistent filter response with movies, total count, and applied filters
     *
     * @param movies the filtered movies
     * @param genre optional genre filter
     * @param rating optional rating filter
     * @param status optional status filter
     * @return Map with movies, total, and appliedFilters
     */
    private Map<String, Object> buildFilterResponse(List<Movie> movies, String genre, String rating, String status) {
        List<MovieDTO> dtos = movies.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> appliedFilters = new HashMap<>();
        appliedFilters.put("genre", genre);
        appliedFilters.put("rating", rating);
        appliedFilters.put("status", status);

        Map<String, Object> result = new HashMap<>();
        result.put("movies", dtos);
        result.put("total", dtos.size());
        result.put("appliedFilters", appliedFilters);

        return result;
    }

    /**
     * Validate genre and throw exception if invalid
     *
     * @param genre the genre to validate
     * @throws InvalidFilterException if invalid
     */
    private void validateGenre(String genre) {
        if (!isValidGenre(genre)) {
            String validValues = String.join(", ", Genre.getAllValues());
            throw InvalidFilterException.forField("genre", genre, validValues);
        }
    }

    /**
     * Validate rating and throw exception if invalid
     *
     * @param rating the rating to validate
     * @throws InvalidFilterException if invalid
     */
    private void validateRating(String rating) {
        if (!isValidRating(rating)) {
            String validValues = String.join(", ", AgeRating.getAllValues());
            throw InvalidFilterException.forField("rating", rating, validValues);
        }
    }

    /**
     * Validate status and throw exception if invalid
     *
     * @param status the status to validate
     * @throws InvalidFilterException if invalid
     */
    private void validateStatus(String status) {
        if (!isValidStatus(status)) {
            String validValues = String.join(", ", MovieStatus.getAllValues());
            throw InvalidFilterException.forField("status", status, validValues);
        }
    }
}
