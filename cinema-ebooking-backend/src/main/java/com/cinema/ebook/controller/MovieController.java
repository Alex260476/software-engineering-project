package com.cinema.ebook.controller;

import com.cinema.ebook.dto.MovieDTO;
import com.cinema.ebook.dto.response.ApiResponse;
import com.cinema.ebook.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MovieController
 *
 * REST API controller for movie-related endpoints.
 * Provides 4 main endpoints for browsing, searching, and filtering movies.
 * All responses wrapped in ApiResponse<T> with consistent format.
 *
 *
 * @author Team 3 Backend
 * @version 2.0
 */
@RestController
@RequestMapping("/api/v1/movies")
@Tag(name = "Movies", description = "Movie browsing, search, and filter endpoints")
@Slf4j
public class MovieController {

    private final MovieService movieService;

    /**
     * Constructor with dependency injection
     *
     * @param movieService the movie service
     */
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    /**
     * Get all movies, optionally filtered by status
     *
     *
     * @param status optional status filter (CURRENTLY_RUNNING or COMING_SOON)
     * @return ResponseEntity with ApiResponse containing list of MovieDTOs
     */
    @GetMapping
    @Operation(summary = "Get all movies",
            description = "Returns all movies or filtered by status (Now Playing/Coming Soon)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved movies",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid status filter provided"
            )
    })
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies(
            @Parameter(description = "Movie status: CURRENTLY_RUNNING or COMING_SOON", required = false)
            @RequestParam(required = false) String status) {

        try {
            log.info("GET /movies - status filter: {}", status);
            List<MovieDTO> movies = movieService.getAllMovies(status);

            String message;
            if (status == null) {
                message = "Retrieved " + movies.size() + " movies";
            } else {
                message = "Retrieved " + movies.size() + " movies with status: " + status;
            }

            ApiResponse<List<MovieDTO>> response = ApiResponse.success(movies, message, movies.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching movies", e);
            ApiResponse<List<MovieDTO>> errorResponse = ApiResponse.error(
                    "Error retrieving movies: " + e.getMessage(),
                    "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Search movies by title
     *
     * Case-insensitive, partial title matching.
     * Feedback addressed: Returns match count and search query in response.
     *
     *
     * @param title the title or partial title to search for
     * @return ResponseEntity with ApiResponse containing search results
     */
    @GetMapping("/search")
    @Operation(summary = "Search movies by title",
            description = "Case-insensitive partial title search")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> searchMovies(
            @Parameter(description = "Title or partial title to search for", required = true)
            @RequestParam(required = true) String title) {

        try {
            log.info("GET /movies/search - title: {}", title);
            Map<String, Object> searchResult = movieService.searchByTitle(title);

            @SuppressWarnings("unchecked")
            List<MovieDTO> movies = (List<MovieDTO>) searchResult.get("movies");
            Integer total = (Integer) searchResult.get("total");
            String query = (String) searchResult.get("query");

            String message;
            if (total == 0) {
                message = "No titles found matching: " + query;
            } else {
                message = "Found " + total + " movie(s) matching: " + query;
            }

            ApiResponse<List<MovieDTO>> response = ApiResponse.success(movies, message, total);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error searching movies", e);
            ApiResponse<List<MovieDTO>> errorResponse = ApiResponse.error(
                    "Error searching movies: " + e.getMessage(),
                    "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Filter movies by genre, rating, and/or status
     *
     * Supports single or multi-criteria filtering.
     * Response includes appliedFilters showing which filters were used.
     *
     * @param genre optional genre filter
     * @param rating optional age rating filter
     * @param status optional status filter
     * @return ResponseEntity with ApiResponse containing filtered movies
     */
    @GetMapping("/filter")
    @Operation(summary = "Filter movies",
            description = "Filter by genre, age rating, and/or status (supports multi-filter)")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> filterMovies(
            @Parameter(description = "Genre filter (Action, Comedy, Drama, Horror, Romance, Sci-Fi, Thriller)", required = false)
            @RequestParam(required = false) String genre,

            @Parameter(description = "Age rating filter (G, PG, PG-13, R)", required = false)
            @RequestParam(required = false) String rating,

            @Parameter(description = "Status filter (CURRENTLY_RUNNING, COMING_SOON)", required = false)
            @RequestParam(required = false) String status) {

        try {
            log.info("GET /movies/filter - genre: {}, rating: {}, status: {}", genre, rating, status);

            Map<String, Object> filterResult;

            // Determine which filter method to call based on provided parameters
            if (genre != null && rating != null && status != null) {
                filterResult = movieService.filterByAllCriteria(genre, rating, status);
            } else if (genre != null && rating != null) {
                filterResult = movieService.filterByGenreAndRating(genre, rating);
            } else if (genre != null && status != null) {
                filterResult = movieService.filterByGenreAndStatus(genre, status);
            } else if (rating != null && status != null) {
                filterResult = movieService.filterByRatingAndStatus(rating, status);
            } else if (genre != null) {
                filterResult = movieService.filterByGenre(genre);
            } else if (rating != null) {
                filterResult = movieService.filterByRating(rating);
            } else if (status != null) {
                filterResult = movieService.filterByStatus(status);
            } else {
                // No filters provided, return all movies
                List<MovieDTO> allMovies = movieService.getAllMovies(null);
                Map<String, Object> allMoviesResult = new HashMap<>();
                allMoviesResult.put("movies", allMovies);
                allMoviesResult.put("total", allMovies.size());
                filterResult = allMoviesResult;
            }

            @SuppressWarnings("unchecked")
            List<MovieDTO> movies = (List<MovieDTO>) filterResult.get("movies");
            Integer total = (Integer) filterResult.get("total");

            @SuppressWarnings("unchecked")
            Map<String, Object> appliedFilters = (Map<String, Object>) filterResult.get("appliedFilters");

            String message;
            if (total == 0) {
                message = "No movies found matching the filter criteria";
            } else {
                message = "Found " + total + " movie(s) matching your filters";
            }

            ApiResponse<List<MovieDTO>> response = ApiResponse.success(movies, message, total, appliedFilters);
            return ResponseEntity.ok(response);

        } catch (com.cinema.ebook.exception.InvalidFilterException e) {
            log.warn("Invalid filter provided: {}", e.getMessage());
            ApiResponse<List<MovieDTO>> errorResponse = ApiResponse.error(e.getMessage(), "INVALID_FILTER");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            log.error("Error filtering movies", e);
            ApiResponse<List<MovieDTO>> errorResponse = ApiResponse.error(
                    "Error filtering movies: " + e.getMessage(),
                    "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all available genres
     *
     * Used by frontend to populate genre filter dropdown.
     *
     * @return ResponseEntity with ApiResponse containing list of genre names
     */
    @GetMapping("/genres")
    @Operation(summary = "Get all available genres",
            description = "Returns list of all available movie genres for filter dropdown")
    public ResponseEntity<ApiResponse<List<String>>> getGenres() {
        try {
            log.info("GET /genres");
            List<String> genres = movieService.getAllGenres();

            ApiResponse<List<String>> response = ApiResponse.success(
                    genres,
                    "Retrieved " + genres.size() + " available genres",
                    genres.size()
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching genres", e);
            ApiResponse<List<String>> errorResponse = ApiResponse.error(
                    "Error retrieving genres: " + e.getMessage(),
                    "ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}
