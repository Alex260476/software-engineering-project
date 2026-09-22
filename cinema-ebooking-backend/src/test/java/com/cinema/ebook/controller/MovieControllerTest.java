package com.cinema.ebook.controller;

import com.cinema.ebook.dto.MovieDTO;
import com.cinema.ebook.dto.response.ApiResponse;
import com.cinema.ebook.service.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MovieControllerTest
 *
 * Comprehensive unit tests for MovieController.
 * Tests all REST endpoints with various scenarios.
 *
 * Coverage:
 * - GET /movies endpoint
 * - GET /movies/search endpoint
 * - GET /movies/filter endpoint
 * - GET /genres endpoint
 * - Parameter validation
 * - Response format
 * - HTTP status codes
 * - Error handling
 *
 * @author Team 3 Backend
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MovieController Tests")
class MovieControllerTest {

    @Mock
    private MovieService movieService;

    @InjectMocks
    private MovieController movieController;

    private MockMvc mockMvc;
    private MovieDTO testMovieDTO;
    private MovieDTO testMovieDTO2;

    /**
     * Setup test data and MockMvc before each test
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(movieController).build();

        testMovieDTO = MovieDTO.builder()
                .id("507f1f77bcf86cd799439011")
                .title("Dune: Part Two")
                .posterUrl("https://test.com/poster.jpg")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action"))
                .status("CURRENTLY_RUNNING")
                .build();

        testMovieDTO2 = MovieDTO.builder()
                .id("507f1f77bcf86cd799439012")
                .title("Avatar: The Way of Water")
                .posterUrl("https://test.com/poster2.jpg")
                .rating("PG-13")
                .genre(Arrays.asList("Sci-Fi", "Action", "Fantasy"))
                .status("CURRENTLY_RUNNING")
                .build();
    }

    // ==================== GET ALL MOVIES TESTS ====================

    @Test
    @DisplayName("Should return all movies without status filter")
    void testGetAllMoviesSuccess() {
        // Arrange
        List<MovieDTO> movies = Arrays.asList(testMovieDTO, testMovieDTO2);
        when(movieService.getAllMovies(null)).thenReturn(movies);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals(2, response.getBody().getTotal());
        verify(movieService, times(1)).getAllMovies(null);
    }

    @Test
    @DisplayName("Should return movies with status filter")
    void testGetMoviesByStatus() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        when(movieService.getAllMovies("CURRENTLY_RUNNING")).thenReturn(movies);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies("CURRENTLY_RUNNING");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(1, response.getBody().getData().size());
        verify(movieService, times(1)).getAllMovies("CURRENTLY_RUNNING");
    }

    @Test
    @DisplayName("Should return empty list for valid status with no results")
    void testGetMoviesEmptyResult() {
        // Arrange
        when(movieService.getAllMovies("COMING_SOON")).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies("COMING_SOON");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().size());
        assertEquals(0, response.getBody().getTotal());
    }

    @Test
    @DisplayName("Should include message in getAllMovies response")
    void testGetAllMoviesMessage() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        when(movieService.getAllMovies(null)).thenReturn(movies);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("movie"));
    }

    // ==================== SEARCH TESTS ====================

    @Test
    @DisplayName("Should search movies by title successfully")
    void testSearchMoviesSuccess() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("movies", movies);
        searchResult.put("total", 1);
        searchResult.put("query", "dune");

        when(movieService.searchByTitle("dune")).thenReturn(searchResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.searchMovies("dune");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals(1, response.getBody().getTotal());
        assertTrue(response.getBody().getMessage().contains("dune"));
        verify(movieService, times(1)).searchByTitle("dune");
    }

    @Test
    @DisplayName("Should return empty search results")
    void testSearchMoviesEmptyResult() {
        // Arrange
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("movies", new ArrayList<>());
        searchResult.put("total", 0);
        searchResult.put("query", "xyz");

        when(movieService.searchByTitle("xyz")).thenReturn(searchResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.searchMovies("xyz");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().size());
        assertTrue(response.getBody().getMessage().contains("No titles found"));
    }

    @Test
    @DisplayName("Should include query in search response")
    void testSearchMoviesIncludesQuery() {
        // Arrange
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("movies", new ArrayList<>());
        searchResult.put("total", 0);
        searchResult.put("query", "avatar");

        when(movieService.searchByTitle("avatar")).thenReturn(searchResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.searchMovies("avatar");

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("avatar"));
    }

    // ==================== FILTER TESTS ====================

    @Test
    @DisplayName("Should filter movies by genre")
    void testFilterByGenre() {
        // Arrange
        List<MovieDTO> movies = Arrays.asList(testMovieDTO, testMovieDTO2);
        Map<String, Object> filterResult = new HashMap<>();
        filterResult.put("movies", movies);
        filterResult.put("total", 2);
        Map<String, Object> appliedFilters = new HashMap<>();
        appliedFilters.put("genre", "Sci-Fi");
        appliedFilters.put("rating", null);
        appliedFilters.put("status", null);
        filterResult.put("appliedFilters", appliedFilters);

        when(movieService.filterByGenre("Sci-Fi")).thenReturn(filterResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Sci-Fi", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertNotNull(response.getBody().getAppliedFilters());
    }

    @Test
    @DisplayName("Should filter by genre and status")
    void testFilterByGenreAndStatus() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        Map<String, Object> filterResult = new HashMap<>();
        filterResult.put("movies", movies);
        filterResult.put("total", 1);
        Map<String, Object> appliedFilters = new HashMap<>();
        appliedFilters.put("genre", "Sci-Fi");
        appliedFilters.put("rating", null);
        appliedFilters.put("status", "CURRENTLY_RUNNING");
        filterResult.put("appliedFilters", appliedFilters);

        when(movieService.filterByGenreAndStatus("Sci-Fi", "CURRENTLY_RUNNING")).thenReturn(filterResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Sci-Fi", null, "CURRENTLY_RUNNING");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertNotNull(response.getBody().getAppliedFilters());
    }

    @Test
    @DisplayName("Should filter by all three criteria")
    void testFilterByAllCriteria() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        Map<String, Object> filterResult = new HashMap<>();
        filterResult.put("movies", movies);
        filterResult.put("total", 1);
        Map<String, Object> appliedFilters = new HashMap<>();
        appliedFilters.put("genre", "Sci-Fi");
        appliedFilters.put("rating", "PG-13");
        appliedFilters.put("status", "CURRENTLY_RUNNING");
        filterResult.put("appliedFilters", appliedFilters);

        when(movieService.filterByAllCriteria("Sci-Fi", "PG-13", "CURRENTLY_RUNNING")).thenReturn(filterResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Sci-Fi", "PG-13", "CURRENTLY_RUNNING");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Sci-Fi", response.getBody().getAppliedFilters().get("genre"));
        assertEquals("PG-13", response.getBody().getAppliedFilters().get("rating"));
        assertEquals("CURRENTLY_RUNNING", response.getBody().getAppliedFilters().get("status"));
    }

    @Test
    @DisplayName("Should return empty filter results")
    void testFilterEmptyResult() {
        // Arrange
        Map<String, Object> filterResult = new HashMap<>();
        filterResult.put("movies", new ArrayList<>());
        filterResult.put("total", 0);
        Map<String, Object> appliedFilters = new HashMap<>();
        appliedFilters.put("genre", "Action");
        appliedFilters.put("rating", null);
        appliedFilters.put("status", null);
        filterResult.put("appliedFilters", appliedFilters);

        when(movieService.filterByGenre("Action")).thenReturn(filterResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Action", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().getData().size());
        assertTrue(response.getBody().getMessage().contains("No movies found"));
    }

    // ==================== GENRES ENDPOINT TESTS ====================

    @Test
    @DisplayName("Should return all genres")
    void testGetGenres() {
        // Arrange
        List<String> genres = Arrays.asList("Action", "Comedy", "Drama", "Horror", "Romance", "Sci-Fi", "Thriller");
        when(movieService.getAllGenres()).thenReturn(genres);

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = movieController.getGenres();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(7, response.getBody().getData().size());
        assertEquals(7, response.getBody().getTotal());
        verify(movieService, times(1)).getAllGenres();
    }

    @Test
    @DisplayName("Should include message in genres response")
    void testGetGenresMessage() {
        // Arrange
        List<String> genres = Arrays.asList("Action", "Comedy");
        when(movieService.getAllGenres()).thenReturn(genres);

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = movieController.getGenres();

        // Assert
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("genre"));
    }

    // ==================== RESPONSE FORMAT TESTS ====================

    @Test
    @DisplayName("Should include success flag in response")
    void testResponseIncludesSuccessFlag() {
        // Arrange
        when(movieService.getAllMovies(null)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertNotNull(response.getBody().getSuccess());
        assertTrue(response.getBody().getSuccess());
    }

    @Test
    @DisplayName("Should include timestamp in response")
    void testResponseIncludesTimestamp() {
        // Arrange
        when(movieService.getAllMovies(null)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    @DisplayName("Should include total count in response")
    void testResponseIncludesTotal() {
        // Arrange
        List<MovieDTO> movies = Arrays.asList(testMovieDTO, testMovieDTO2);
        when(movieService.getAllMovies(null)).thenReturn(movies);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertNotNull(response.getBody().getTotal());
        assertEquals(2, response.getBody().getTotal());
    }

    // ==================== DTO CONTENT TESTS ====================

    @Test
    @DisplayName("Should return MovieDTO with required fields")
    void testMovieDTOHasRequiredFields() {
        // Arrange
        List<MovieDTO> movies = Collections.singletonList(testMovieDTO);
        when(movieService.getAllMovies(null)).thenReturn(movies);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        MovieDTO dto = response.getBody().getData().get(0);
        assertNotNull(dto.getId());
        assertNotNull(dto.getTitle());
        assertNotNull(dto.getPosterUrl());
        assertNotNull(dto.getRating());
        assertNotNull(dto.getGenre());
        assertNotNull(dto.getStatus());
    }

    // ==================== ERROR SCENARIO TESTS ====================

    @Test
    @DisplayName("Should handle exception in getAllMovies gracefully")
    void testGetAllMoviesException() {
        // Arrange
        when(movieService.getAllMovies(any())).thenThrow(new RuntimeException("Database error"));

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
        assertTrue(response.getBody().getMessage().contains("Error"));
    }

    @Test
    @DisplayName("Should handle exception in searchMovies gracefully")
    void testSearchMoviesException() {
        // Arrange
        when(movieService.searchByTitle(any())).thenThrow(new RuntimeException("Search error"));

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.searchMovies("test");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    @DisplayName("Should handle exception in filterMovies gracefully")
    void testFilterMoviesException() {
        // Arrange
        when(movieService.filterByGenre(any())).thenThrow(new RuntimeException("Filter error"));

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Action", null, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
    }

    @Test
    @DisplayName("Should handle exception in getGenres gracefully")
    void testGetGenresException() {
        // Arrange
        when(movieService.getAllGenres()).thenThrow(new RuntimeException("Genres error"));

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = movieController.getGenres();

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().getSuccess());
    }

    // ==================== HTTP STATUS CODE TESTS ====================

    @Test
    @DisplayName("Should return 200 OK for successful getAllMovies")
    void testGetAllMoviesStatusCode() {
        // Arrange
        when(movieService.getAllMovies(null)).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.getAllMovies(null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 200 OK for successful search")
    void testSearchMoviesStatusCode() {
        // Arrange
        Map<String, Object> searchResult = new HashMap<>();
        searchResult.put("movies", new ArrayList<>());
        searchResult.put("total", 0);
        searchResult.put("query", "test");

        when(movieService.searchByTitle("test")).thenReturn(searchResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.searchMovies("test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 200 OK for successful filter")
    void testFilterMoviesStatusCode() {
        // Arrange
        Map<String, Object> filterResult = new HashMap<>();
        filterResult.put("movies", new ArrayList<>());
        filterResult.put("total", 0);
        filterResult.put("appliedFilters", new HashMap<>());

        when(movieService.filterByGenre("Action")).thenReturn(filterResult);

        // Act
        ResponseEntity<ApiResponse<List<MovieDTO>>> response = movieController.filterMovies("Action", null, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return 200 OK for genres endpoint")
    void testGetGenresStatusCode() {
        // Arrange
        when(movieService.getAllGenres()).thenReturn(new ArrayList<>());

        // Act
        ResponseEntity<ApiResponse<List<String>>> response = movieController.getGenres();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
