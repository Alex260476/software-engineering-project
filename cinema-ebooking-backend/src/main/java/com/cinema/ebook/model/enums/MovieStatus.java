package com.cinema.ebook.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MovieStatus Enum
 *
 * Defines the available statuses for movies in the Cinema E-Booking System.
 *
 * Status meanings:
 * - CURRENTLY_RUNNING: Movie is actively showing in the theater (show home page "Now Playing" section)
 * - COMING_SOON: Movie will be available in the future (show home page "Coming Soon" section)
 *
 *
 * @author Team 3 Backend
 * @version 1.0
 */
public enum MovieStatus {

    /**
     * Movie is currently playing in the theater
     * Home page should display with available showtimes
     */
    CURRENTLY_RUNNING("CURRENTLY_RUNNING", "Now Playing"),

    /**
     * Movie will be available soon
     * Home page should display in separate section without showtimes
     */
    COMING_SOON("COMING_SOON", "Coming Soon");

    /**
     * The database value for this status
     */
    private final String value;

    /**
     * The human-readable display name
     */
    private final String displayName;

    /**
     * Constructor
     *
     * @param value the database value
     * @param displayName the display name for UI
     */
    MovieStatus(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    /**
     * Get the database value for this status
     *
     * @return the value used in database queries
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the human-readable display name
     *
     * @return the display name for UI rendering
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Convert a string value to the corresponding MovieStatus enum
     * Case-insensitive and handles both enum name and database value
     *
     * @param value the string representation
     * @return the corresponding MovieStatus
     * @throws IllegalArgumentException if value is not valid
     */
    public static MovieStatus fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Status value cannot be null or empty");
        }

        String upperValue = value.toUpperCase();

        for (MovieStatus status : MovieStatus.values()) {
            if (status.value.equals(upperValue) || status.name().equals(upperValue)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Invalid status: " + value + ". Valid values: " + getAllValues());
    }

    /**
     * Get all valid status values as a list of strings
     * Used for validation and error messages
     *
     * @return List of all valid status values
     */
    public static List<String> getAllValues() {
        return Arrays.stream(MovieStatus.values())
                .map(MovieStatus::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Check if a status string is valid
     *
     * @param value the value to check
     * @return true if valid status value
     */
    public static boolean isValid(String value) {
        try {
            fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
