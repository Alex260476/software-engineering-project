package com.cinema.ebook.exception;

import com.cinema.ebook.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * GlobalExceptionHandler
 *
 * Centralized exception handling for the entire application.
 * Catches exceptions thrown by controllers and services, and returns appropriate HTTP responses.
 * Ensures consistent error response format across all endpoints.
 *
 * @author Team 3 Backend
 * @version 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle InvalidFilterException
     * Thrown when an invalid filter value is provided to an endpoint
     *
     * @param ex the exception
     * @return ResponseEntity with 400 Bad Request and error details
     */
    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFilterException(InvalidFilterException ex) {
        log.warn("Invalid filter exception: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "INVALID_FILTER"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle ResourceNotFoundException
     * Thrown when a requested resource is not found
     *
     * @param ex the exception
     * @return ResponseEntity with 404 Not Found and error details
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                ex.getMessage(),
                "RESOURCE_NOT_FOUND"
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handle IllegalArgumentException
     * Thrown for invalid arguments to methods
     *
     * @param ex the exception
     * @return ResponseEntity with 400 Bad Request and error details
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid argument: " + ex.getMessage(),
                "INVALID_ARGUMENT"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle NullPointerException
     * Thrown when unexpected null values are encountered
     *
     * @param ex the exception
     * @return ResponseEntity with 400 Bad Request and generic error
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Object>> handleNullPointerException(NullPointerException ex) {
        log.error("Null pointer exception", ex);

        ApiResponse<Object> response = ApiResponse.error(
                "Invalid request data",
                "NULL_POINTER"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handle all other exceptions (catch-all)
     * Logs the error and returns generic server error response
     *
     * @param ex the exception
     * @return ResponseEntity with 500 Internal Server Error and error details
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        log.error("Unexpected exception", ex);

        ApiResponse<Object> response = ApiResponse.error(
                "An unexpected error occurred. Please try again later.",
                "INTERNAL_SERVER_ERROR"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
