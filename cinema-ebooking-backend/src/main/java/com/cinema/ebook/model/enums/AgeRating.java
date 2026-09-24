package com.cinema.ebook.model.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AgeRating Enum
 *
 * Defines MPAA age ratings for movies in the Cinema E-Booking System.
 *
 * Ratings:
 * - G: General Audiences - suitable for all ages
 * - PG: Parental Guidance Suggested - some material may not be suitable for children
 * - PG-13: Parents Strongly Cautioned - some material may be inappropriate for children under 13
 * - R: Restricted - children under 17 require parent/guardian for attendance
 *
 * @author Team 3 Backend
 * @version 1.0
 */
public enum AgeRating {
    G("G", 0),
    PG("PG", 0),
    PG_13("PG-13", 0),
    R("R", 17);

    private final String value;
    private final int minimumAge;

    AgeRating(String value, int minimumAge) {
        this.value = value;
        this.minimumAge = minimumAge;
    }

    public String getValue() {
        return value;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public static AgeRating fromValue(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Rating value cannot be null or empty");
        }

        for (AgeRating rating : AgeRating.values()) {
            if (rating.value.equals(value)) {
                return rating;
            }
        }

        throw new IllegalArgumentException("Invalid rating: " + value + ". Valid values: " + getAllValues());
    }

    public static List<String> getAllValues() {
        return Arrays.stream(AgeRating.values())
                .map(AgeRating::getValue)
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
