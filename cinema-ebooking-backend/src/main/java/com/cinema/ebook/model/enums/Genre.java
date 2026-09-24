package com.cinema.ebook.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Genre Enum
 *
 * Defines all available movie genres in the Cinema E-Booking System.
 * Movies can have multiple genres.
 *
 * @author Team 3 Backend
 * @version 1.0
 */
public enum Genre {
    ACTION("Action"),
    COMEDY("Comedy"),
    DRAMA("Drama"),
    HORROR("Horror"),
    ROMANCE("Romance"),
    SCI_FI("Sci-Fi"),
    THRILLER("Thriller");

    private final String displayName;

    Genre(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Genre fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Genre value cannot be null or empty");
        }

        String upperValue = value.replaceAll("-", "_").toUpperCase();

        try {
            return Genre.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid genre: " + value + ". Valid values: " + getAllValues());
        }
    }

    public static List<String> getAllValues() {
        return Arrays.stream(Genre.values())
                .map(Genre::getDisplayName)
                .collect(Collectors.toList());
    }

    public static boolean isValid(String value) {
        try {
            fromValue(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
