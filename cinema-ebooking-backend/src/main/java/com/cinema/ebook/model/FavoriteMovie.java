package com.cinema.ebook.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * FavoriteMovie Entity
 * Tracks user's favorite movies with timestamp
 * Enables analytics on popular movies and user preferences
 */
@Entity
@Table(name = "favorite_movies", indexes = {
    @Index(name = "idx_favorite_user", columnList = "user_id"),
    @Index(name = "idx_favorite_movie", columnList = "movie_id"),
    @Index(name = "idx_favorite_unique", columnList = {"user_id", "movie_id"}, unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteMovie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(nullable = false, updatable = false)
    private LocalDateTime favoritedAt = LocalDateTime.now();

    /**
     * Optional: Reason for favoriting (user comment)
     */
    private String note;

    /**
     * Track if user is still interested
     */
    private boolean isActive = true;

    // Utility Methods
    /**
     * Deactivate favorite without deleting
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Reactivate favorite
     */
    public void reactivate() {
        this.isActive = true;
    }

    /**
     * Get days since favorited
     */
    public long getDaysSinceFavorited() {
        return java.time.temporal.ChronoUnit.DAYS.between(
            favoritedAt,
            LocalDateTime.now()
        );
    }
}
