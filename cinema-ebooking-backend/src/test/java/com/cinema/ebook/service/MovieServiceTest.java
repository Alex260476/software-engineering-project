package com.cinema.ebook.service;

import com.cinema.ebook.model.Movie;
import com.cinema.ebook.model.enums.AgeRating;
import com.cinema.ebook.model.enums.Genre;
import com.cinema.ebook.model.enums.MovieStatus;
import com.cinema.ebook.dto.MovieDTO;
import com.cinema.ebook.exception.InvalidFilterException;
import com.cinema.ebook.exception.ResourceNotFoundException;
import com.cinema.ebook.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MovieServiceTest
 *
 * Comprehensive unit tests for MovieService implementation.
 * Tests all search, filter, and utility methods with various scenarios.
 *
 * Coverage:
 * - getAllMovies() with and without status
 * - Search functionality (success and empty cases)
 * - Filter operations (single and multi-filter)
 * - Input validation
 * - Error handling
 *
 * @author Team 3 Backend
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie testMovie;
    private Movie testMovie2;

    /**
     * Setup test data before each test
     */
    @BeforeEach
    void setUp() {
        testMovie = Movie.builder()
                .id("507f1f77bcf86cd799439011")
                .title("Dune: Part Two")
                .description("Test description")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action"))
                .runtime(166)
                .releaseDate("2024-02-26")
                .posterUrl("https://test.com/poster.jpg")
                .trailerUrl("https://youtube.com/embed/test")
                .director("Denis Villeneuve")
                .cast(Arrays.asList("Actor 1", "Actor 2"))
                .imdbRating(8.5)
                .status("CURRENTLY_RUNNING")
                .build();

        testMovie2 = Movie.builder()
                .id("507f1f77bcf86cd799439012")
                .title("Avatar: The Way of Water")
                .description("Test description 2")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Fantasy"))
                .runtime(192)
                .releaseDate("2022-12-16")
                .posterUrl("https://test.com/poster2.jpg")
                .trailerUrl("https://youtube.com/embed/test2")
                .director("James Cameron")
                .cast(Arrays.asList("Actor 3", "Actor 4"))
                .imdbRating(7.8)
                .status("CURRENTLY_RUNNING")
                .build();
    }

    // ==================== GET ALL MOVIES TESTS ====================

    @Test
    @DisplayName("Should return all movies when status is null")
    void testGetAllMoviesWithoutStatus() {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie, testMovie2);
        when(movieRepository.findAll()).thenReturn(movies);

        // Act
        List<MovieDTO> result = movieService.getAllMovies(null);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Dune: Part Two", result.get(0).getTitle());
        verify(movieRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return movies with specific status")
    void testGetAllMoviesByStatus() {
        // Arrange
        List<Movie> movies = Collections.singletonList(testMovie);
        when(movieRepository.findByStatus("CURRENTLY_RUNNING")).thenReturn(movies);

        // Act
        List<MovieDTO> result = movieService.getAllMovies("CURRENTLY_RUNNING");

        // Assert
        assertEquals(1, result.size());
        assertEquals("Dune: Part Two", result.get(0).getTitle());
        verify(movieRepository, times(1)).findByStatus("CURRENTLY_RUNNING");
    }

    @Test
    @DisplayName("Should throw exception for invalid status")
    void testGetAllMoviesWithInvalidStatus() {
        // Act & Assert
        assertThrows(InvalidFilterException.class, () -> {
            movieService.getAllMovies("INVALID_STATUS");
        });
    }

    @Test
    @DisplayName("Should return empty list when no movies match status")
    void testGetAllMoviesEmptyResult() {
        // Arrange
        when(movieRepository.findByStatus("COMING_SOON")).thenReturn(new ArrayList<>());

        // Act
        List<MovieDTO> result = movieService.getAllMovies("COMING_SOON");

        // Assert
        assertEquals(0, result.size());
    }

    // ==================== SEARCH TESTS ====================

    @Test
    @DisplayName("Should search movies by title successfully")
    void testSearchByTitleSuccess() {
        // Arrange
        List<Movie> movies = Collections.singletonList(testMovie);
        when(movieRepository.findByTitleIgnoreCaseContaining("dune")).thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.searchByTitle("dune");

        // Assert
        assertNotNull(result);
        assertEquals(1, ((List<?>) result.get("movies")).size());
        assertEquals(1, result.get("total"));
        assertEquals("dune", result.get("query"));
        verify(movieRepository, times(1)).findByTitleIgnoreCaseContaining("dune");
    }

    @Test
    @DisplayName("Should return empty result for non-matching search")
    void testSearchByTitleEmpty() {
        // Arrange
        when(movieRepository.findByTitleIgnoreCaseContaining("xyz")).thenReturn(new ArrayList<>());

        // Act
        Map<String, Object> result = movieService.searchByTitle("xyz");

        // Assert
        assertEquals(0, ((List<?>) result.get("movies")).size());
        assertEquals(0, result.get("total"));
    }

    @Test
    @DisplayName("Should handle null search query")
    void testSearchByTitleNull() {
        // Act
        Map<String, Object> result = movieService.searchByTitle(null);

        // Assert
        assertEquals(0, ((List<?>) result.get("movies")).size());
    }

    // ==================== FILTER TESTS ====================

    @Test
    @DisplayName("Should filter movies by genre")
    void testFilterByGenre() {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie, testMovie2);
        when(movieRepository.findByGenreContaining("Sci-Fi")).thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.filterByGenre("Sci-Fi");

        // Assert
        assertEquals(2, ((List<?>) result.get("movies")).size());
        assertEquals(2, result.get("total"));

        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) result.get("appliedFilters");
        assertEquals("Sci-Fi", filters.get("genre"));
        assertNull(filters.get("rating"));
        assertNull(filters.get("status"));
    }

    @Test
    @DisplayName("Should throw exception for invalid genre")
    void testFilterByInvalidGenre() {
        // Act & Assert
        assertThrows(InvalidFilterException.class, () -> {
            movieService.filterByGenre("InvalidGenre");
        });
    }

    @Test
    @DisplayName("Should filter by genre and status")
    void testFilterByGenreAndStatus() {
        // Arrange
        List<Movie> movies = Collections.singletonList(testMovie);
        when(movieRepository.findByGenreContainingAndStatus("Sci-Fi", "CURRENTLY_RUNNING"))
                .thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.filterByGenreAndStatus("Sci-Fi", "CURRENTLY_RUNNING");

        // Assert
        assertEquals(1, ((List<?>) result.get("movies")).size());

        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) result.get("appliedFilters");
        assertEquals("Sci-Fi", filters.get("genre"));
        assertEquals("CURRENTLY_RUNNING", filters.get("status"));
    }

    @Test
    @DisplayName("Should filter by all three criteria")
    void testFilterByAllCriteria() {
        // Arrange
        List<Movie> movies = Collections.singletonList(testMovie);
        when(movieRepository.findByGenreContainingAndRatingAndStatus("Sci-Fi", "PG-13", "CURRENTLY_RUNNING"))
                .thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.filterByAllCriteria("Sci-Fi", "PG-13", "CURRENTLY_RUNNING");

        // Assert
        assertEquals(1, ((List<?>) result.get("movies")).size());

        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) result.get("appliedFilters");
        assertEquals("Sci-Fi", filters.get("genre"));
        assertEquals("PG-13", filters.get("rating"));
        assertEquals("CURRENTLY_RUNNING", filters.get("status"));
    }

    @Test
    @DisplayName("Should filter running movies by the day of week of a show date")
    void testFilterByShowDate() {
        // 2026-09-26 is a Saturday
        when(movieRepository.findByStatusAndShowDaysIn("CURRENTLY_RUNNING", Set.of("SATURDAY")))
                .thenReturn(Collections.singletonList(testMovie));

        Map<String, Object> result = movieService.filterByShowDates(List.of("2026-09-26"), null);

        assertEquals(1, result.get("total"));
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) result.get("appliedFilters");
        assertEquals(List.of("2026-09-26"), filters.get("showDate"));
        assertEquals("CURRENTLY_RUNNING", filters.get("status"));
    }

    @Test
    @DisplayName("Should combine several show dates and a normalized genre")
    void testFilterByShowDatesAndGenre() {
        // Weekend: Saturday 2026-09-26 and Sunday 2026-09-27
        when(movieRepository.findByStatusAndShowDaysInAndGenreContaining(
                "CURRENTLY_RUNNING", Set.of("SATURDAY", "SUNDAY"), "Sci-Fi"))
                .thenReturn(Arrays.asList(testMovie, testMovie2));

        Map<String, Object> result = movieService.filterByShowDates(List.of("2026-09-26", "2026-09-27"), "sci-fi");

        assertEquals(2, result.get("total"));
    }

    @Test
    @DisplayName("Should reject a malformed show date")
    void testFilterByInvalidShowDate() {
        assertThrows(InvalidFilterException.class,
                () -> movieService.filterByShowDates(List.of("tomorrow"), null));
    }

    // ==================== UTILITY TESTS ====================

    @Test
    @DisplayName("Should get all genres")
    void testGetAllGenres() {
        // Act
        List<String> genres = movieService.getAllGenres();

        // Assert
        assertNotNull(genres);
        assertEquals(7, genres.size());
        assertTrue(genres.contains("Action"));
        assertTrue(genres.contains("Sci-Fi"));
        assertTrue(genres.contains("Comedy"));
    }

    @Test
    @DisplayName("Should get movie by ID")
    void testGetMovieById() {
        // Arrange
        when(movieRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMovie));

        // Act
        Movie result = movieService.getMovieById("507f1f77bcf86cd799439011");

        // Assert
        assertNotNull(result);
        assertEquals("Dune: Part Two", result.getTitle());
        verify(movieRepository, times(1)).findById("507f1f77bcf86cd799439011");
    }

    @Test
    @DisplayName("Should get movie details DTO by ID")
    void testGetMovieDetails() {
        when(movieRepository.findById("507f1f77bcf86cd799439011")).thenReturn(Optional.of(testMovie));

        MovieDTO result = movieService.getMovieDetails("507f1f77bcf86cd799439011");

        assertEquals("Dune: Part Two", result.getTitle());
        assertEquals("https://youtube.com/embed/test", result.getTrailerUrl());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for missing movie details")
    void testGetMovieDetailsNotFound() {
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieDetails("nonexistent"));
    }

    @Test
    @DisplayName("Should query the database with the canonical rating value")
    void testRatingFilterNormalized() {
        when(movieRepository.findByRating("PG-13")).thenReturn(Collections.singletonList(testMovie));

        Map<String, Object> result = movieService.filterByRating("pg-13");

        assertEquals(1, result.get("total"));
    }

    @Test
    @DisplayName("Should return null for non-existent movie ID")
    void testGetMovieByIdNotFound() {
        // Arrange
        when(movieRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act
        Movie result = movieService.getMovieById("nonexistent");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should check if movie exists")
    void testMovieExists() {
        // Arrange
        when(movieRepository.existsByTitleIgnoreCase("Dune: Part Two")).thenReturn(true);

        // Act
        boolean result = movieService.movieExists("Dune: Part Two");

        // Assert
        assertTrue(result);
        verify(movieRepository, times(1)).existsByTitleIgnoreCase("Dune: Part Two");
    }

    @Test
    @DisplayName("Should count movies by status")
    void testCountByStatus() {
        // Arrange
        when(movieRepository.countByStatus("CURRENTLY_RUNNING")).thenReturn(5L);

        // Act
        long result = movieService.countByStatus("CURRENTLY_RUNNING");

        // Assert
        assertEquals(5L, result);
    }

    // ==================== VALIDATION TESTS ====================

    @Test
    @DisplayName("Should validate valid genre")
    void testIsValidGenre() {
        // Act & Assert
        assertTrue(movieService.isValidGenre("Action"));
        assertTrue(movieService.isValidGenre("Sci-Fi"));
        assertTrue(movieService.isValidGenre("action")); // Case-insensitive
        assertFalse(movieService.isValidGenre("InvalidGenre"));
        assertFalse(movieService.isValidGenre(null));
    }

    @Test
    @DisplayName("Should validate valid rating")
    void testIsValidRating() {
        // Act & Assert
        assertTrue(movieService.isValidRating("PG-13"));
        assertTrue(movieService.isValidRating("R"));
        assertTrue(movieService.isValidRating("pg-13")); // Case-insensitive
        assertFalse(movieService.isValidRating("X"));
        assertFalse(movieService.isValidRating(null));
    }

    @Test
    @DisplayName("Should validate valid status")
    void testIsValidStatus() {
        // Act & Assert
        assertTrue(movieService.isValidStatus("CURRENTLY_RUNNING"));
        assertTrue(movieService.isValidStatus("COMING_SOON"));
        assertTrue(movieService.isValidStatus("currently_running")); // Case-insensitive
        assertFalse(movieService.isValidStatus("INVALID"));
        assertFalse(movieService.isValidStatus(null));
    }

    // ==================== DTO CONVERSION TESTS ====================

    @Test
    @DisplayName("Should convert Movie to MovieDTO")
    void testMovieToDTOConversion() {
        // Act
        List<MovieDTO> dtos = movieService.getAllMovies(null);

        // Setup mock to return our test movies for this test
        when(movieRepository.findAll()).thenReturn(Collections.singletonList(testMovie));
        dtos = movieService.getAllMovies(null);

        // Assert
        assertEquals(1, dtos.size());
        MovieDTO dto = dtos.get(0);
        assertEquals("Dune: Part Two", dto.getTitle());
        assertEquals("507f1f77bcf86cd799439011", dto.getId());
        assertEquals("PG-13", dto.getRating());
        assertEquals(2, dto.getGenre().size());
    }

    // ==================== RESPONSE MAP TESTS ====================

    @Test
    @DisplayName("Should include applied filters in response")
    void testAppliedFiltersInResponse() {
        // Arrange
        List<Movie> movies = Collections.singletonList(testMovie);
        when(movieRepository.findByGenreContainingAndStatus("Sci-Fi", "CURRENTLY_RUNNING"))
                .thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.filterByGenreAndStatus("Sci-Fi", "CURRENTLY_RUNNING");

        // Assert
        assertNotNull(result.get("appliedFilters"));
        @SuppressWarnings("unchecked")
        Map<String, Object> filters = (Map<String, Object>) result.get("appliedFilters");
        assertEquals("Sci-Fi", filters.get("genre"));
        assertEquals("CURRENTLY_RUNNING", filters.get("status"));
        assertNull(filters.get("rating"));
    }

    @Test
    @DisplayName("Should include query in search response")
    void testQueryInSearchResponse() {
        // Arrange
        when(movieRepository.findByTitleIgnoreCaseContaining("avatar")).thenReturn(Collections.emptyList());

        // Act
        Map<String, Object> result = movieService.searchByTitle("avatar");

        // Assert
        assertEquals("avatar", result.get("query"));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle whitespace in search")
    void testSearchWithWhitespace() {
        // Arrange
        when(movieRepository.findByTitleIgnoreCaseContaining("dune")).thenReturn(new ArrayList<>());

        // Act
        Map<String, Object> result = movieService.searchByTitle("  dune  ");

        // Assert
        assertEquals("dune", result.get("query"));
    }

    @Test
    @DisplayName("Should handle case-insensitive genre filter")
    void testGenreFilterCaseInsensitive() {
        // Arrange
        when(movieRepository.findByGenreContaining("Action")).thenReturn(new ArrayList<>());

        // Act
        // This should work because genre validation is case-insensitive
        assertDoesNotThrow(() -> movieService.filterByGenre("action"));
    }

    @Test
    @DisplayName("Should handle multiple movies with same genre")
    void testMultipleMoviesSameGenre() {
        // Arrange
        List<Movie> movies = Arrays.asList(testMovie, testMovie2);
        when(movieRepository.findByGenreContaining("Sci-Fi")).thenReturn(movies);

        // Act
        Map<String, Object> result = movieService.filterByGenre("Sci-Fi");

        // Assert
        assertEquals(2, ((List<?>) result.get("movies")).size());
        assertEquals(2, result.get("total"));
    }
}
