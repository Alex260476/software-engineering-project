package com.cinema.ebook.model;

import com.cinema.ebook.model.enums.AgeRating;
import com.cinema.ebook.model.enums.Genre;
import com.cinema.ebook.model.enums.MovieStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Movie Entity
 *
 * Represents a movie in the Cinema E-Booking System with full metadata.
 * Supports both "Now Playing" and "Coming Soon" statuses.
 *
 *
 * @author Team 3 Backend
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "movies")
public class Movie {

    /**
     * MongoDB unique identifier
     */
    @Id
    private String id;

    /**
     * Movie title (unique)
     */
    private String title;

    /**
     * Detailed movie synopsis
     */
    private String description;

    /**
     * Age rating using AgeRating enum (G, PG, PG-13, R)
     */
    private String rating;

    /**
     * List of genres for this movie (supports multiple genres)
     * Valid values: ACTION, COMEDY, DRAMA, HORROR, ROMANCE, SCI_FI, THRILLER
     */
    private List<String> genre;

    /**
     * Runtime in minutes
     */
    private Integer runtime;

    /**
     * Release date as string (YYYY-MM-DD format)
     */
    private String releaseDate;

    /**
     * URL to movie poster image
     */
    private String posterUrl;

    /**
     * URL to movie trailer (YouTube embed format)
     */
    private String trailerUrl;

    /**
     * Movie director name
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
     * Movie status: CURRENTLY_RUNNING or COMING_SOON
     *
     */
    private String status;

    /**
     * Available showtimes for this movie (if CURRENTLY_RUNNING)
     */
    private List<String> availableShowtimes;

    /**
     * Available seat count across all showtimes
     * Used to indicate availability on home page
     */
    private Integer totalAvailableSeats;

    /**
     * Timestamp when movie record was created
     * Set automatically via @PrePersist
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when movie record was last updated
     * Set automatically via @PreUpdate
     */
    private LocalDateTime updatedAt;

    /**
     * Pre-persist hook: set createdAt and updatedAt timestamps
     * MongoDB doesn't automatically manage these, so we do it in code
     */
    @org.springframework.data.mongodb.core.mapping.event.PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this movie has available showtimes
     * Used on home page to determine if "Now Playing" section should show this movie
     *
     * @return true if status is CURRENTLY_RUNNING and showtimes exist
     */
    @Transient
    public boolean hasAvailableShowtimes() {
        return MovieStatus.CURRENTLY_RUNNING.getValue().equals(this.status)
                && this.availableShowtimes != null
                && !this.availableShowtimes.isEmpty();
    }

    /**
     * Gets display string for showtimes
     * Used by home page to show "Now Playing at: 2:00 PM, 5:00 PM, 8:00 PM"
     *
     * @return formatted showtime string or empty string if not available
     */
    @Transient
    public String getShowtimeDisplay() {
        if (!hasAvailableShowtimes()) {
            return "";
        }
        return String.join(", ", this.availableShowtimes);
    }
}
