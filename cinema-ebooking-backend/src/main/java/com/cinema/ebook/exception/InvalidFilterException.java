package com.cinema.ebook.exception;

/**
 * InvalidFilterException
 *
 * Thrown when an invalid filter value is provided to an API endpoint.
 * Provides detailed information about valid values for error messages.
 *
 * @author Team 3 Backend
 * @version 1.0
 */
public class InvalidFilterException extends RuntimeException {

    private static final String ERROR_CODE = "INVALID_FILTER";

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public InvalidFilterException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public InvalidFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the error code for this exception
     *
     * @return "INVALID_FILTER"
     */
    public String getErrorCode() {
        return ERROR_CODE;
    }

    /**
     * Helper to create exception with valid values information
     *
     * @param fieldName the filter field name (e.g., "genre")
     * @param providedValue the invalid value provided
     * @param validValues comma-separated list of valid values
     * @return InvalidFilterException with detailed message
     */
    public static InvalidFilterException forField(String fieldName, String providedValue, String validValues) {
        String message = String.format(
                "Invalid %s: '%s'. Valid values: %s",
                fieldName, providedValue, validValues
        );
        return new InvalidFilterException(message);
    }
}
