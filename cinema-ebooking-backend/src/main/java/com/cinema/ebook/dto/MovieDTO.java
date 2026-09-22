package com.cinema.ebook.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MovieDTO (Data Transfer Object)
 *
 * Used for API responses when returning movie data to the client.
 * Contains all necessary information for movie display on frontend.
 *
 *
 * @author Team 3 Backend
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDTO {

    /**
     * Movie unique identifier
     */
    private String id;

    /**
     * Movie title
     */
    private String title;

    /**
     * Movie poster image URL
     */
    private String posterUrl;

    /**
     * Age rating (G, PG, PG-13, R)
     */
    private String rating;

    /**
     * List of genres
     */
    private List<String> genre;

    /**
     * Movie status (CURRENTLY_RUNNING, COMING_SOON)
     */
    private String status;

    /**
     * Movie description/synopsis
     */
    private String description;

    /**
     * Movie trailer URL (YouTube embed)
     */
    private String trailerUrl;

    /**
     * Runtime in minutes
     */
    private Integer runtime;

    /**
     * Release date (YYYY-MM-DD format)
     */
    private String releaseDate;

    /**
     * Director name
     */
    private String director;

    /**
     * List of cast members
     */
    private List<String> cast;

    /**
     * IMDb rating (0.0 - 10.0)
     */
    private Double imdbRating;

    /**
     * Available showtimes for this movie
     * Format: ["14:00", "17:00", "20:00"]
     */
    private List<String> availableShowtimes;

    /**
     * Total available seats across all showtimes
     * Used to indicate availability on home page
     */
    private Integer totalAvailableSeats;

    /**
     * Timestamp when movie was added
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when movie was last updated
     */
    private LocalDateTime updatedAt;
}
