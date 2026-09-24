package com.cinema.ebook.exception;

/**
 * ResourceNotFoundException
 *
 * Thrown when a requested resource (e.g., movie by ID) is not found in the database.
 * Maps to HTTP 404 Not Found status code.
 *
 * @author Team 3 Backend
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    /**
     * Constructor with message
     *
     * @param message the error message
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor with message and cause
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Get the error code for this exception
     *
     * @return "RESOURCE_NOT_FOUND"
     */
    public String getErrorCode() {
        return ERROR_CODE;
    }

    /**
     * Helper to create exception for resource not found
     *
     * @param resourceType the type of resource (e.g., "Movie")
     * @param identifier the identifier that was searched for (e.g., movie ID)
     * @return ResourceNotFoundException with detailed message
     */
    public static ResourceNotFoundException forResource(String resourceType, String identifier) {
        String message = String.format("%s not found with identifier: %s", resourceType, identifier);
        return new ResourceNotFoundException(message);
    }
}
