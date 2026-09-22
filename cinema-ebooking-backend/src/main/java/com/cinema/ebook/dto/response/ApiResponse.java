package com.cinema.ebook.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * ApiResponse<T> - Generic API Response Wrapper
 *
 * All API endpoints return data wrapped in this class to provide consistent response format.
 * Structure: { success, data, message, error, total, appliedFilters, timestamp }
 *
 * @author Team 3 Backend
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Success indicator (true = successful request, false = error)
     */
    private Boolean success;

    /**
     * Response data (could be List, Object, String, etc.)
     */
    private T data;

    /**
     * User-friendly message describing the response
     */
    private String message;

    /**
     * Error code for failed requests (e.g., "INVALID_FILTER", "RESOURCE_NOT_FOUND")
     */
    private String error;

    /**
     * Total count of items (for paginated or search results)
     */
    private Integer total;

    /**
     * Applied filters for search/filter responses
     * Shows which filters were used: {genre: "Action", rating: "PG-13", status: "CURRENTLY_RUNNING"}
     */
    private Map<String, Object> appliedFilters;

    /**
     * Server timestamp of the response (ISO 8601 format)
     */
    private LocalDateTime timestamp;

    // ==================== FACTORY METHODS ====================

    /**
     * Create a successful response with data and message
     *
     * @param data the response data
     * @param message the success message
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response with data, message, and total count
     * Used for search results
     *
     * @param data the response data
     * @param message the success message
     * @param total the total count
     * @return ApiResponse with total field set
     */
    public static <T> ApiResponse<T> success(T data, String message, Integer total) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .total(total)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create a successful response with all fields (for complex responses)
     * Used for filtered results with applied filters
     *
     * @param data the response data
     * @param message the success message
     * @param total the total count
     * @param appliedFilters the filters that were applied
     * @return ApiResponse with all fields set
     */
    public static <T> ApiResponse<T> success(T data, String message, Integer total, Map<String, Object> appliedFilters) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .total(total)
                .appliedFilters(appliedFilters)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create an error response
     *
     * @param message the error message
     * @param errorCode the error code
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
