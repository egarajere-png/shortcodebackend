package com.abcbank.shortcode.shortcode.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for API error responses.
 * 
 * This DTO defines the standard format for error responses returned by the API.
 * It ensures consistent error information across all endpoints and error scenarios.
 * 
 * Response Format:
 * HTTP status codes convey the error category, while this object provides
 * detailed error information suitable for client-side error handling.
 * 
 * Usage:
 * - Returned by GlobalExceptionHandler for unhandled exceptions
 * - Used by controller methods to return specific errors
 * - JSON serialized in API responses
 * 
 * Example Response:
 * ```json
 * {
 *   "statusCode": "500",
 *   "message": "An unexpected error occurred"
 * }
 * ```
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    /**
     * Application-specific error code for client-side error handling.
     * 
     * Status Code Format:
     * Usually mirrors HTTP status codes but can be application-specific:
     * - "500": Internal server error
     * - "400": Bad request/validation error
     * - "401": Unauthorized
     * - "403": Forbidden/access denied
     * - "404": Not found
     * 
     * Enables clients to distinguish between different error types
     * and handle them appropriately in UI error messaging.
     */
    private String statusCode;

    /**
     * Human-readable error message for client-side display.
     * 
     * Message Guidelines:
     * - Should not expose sensitive system information
     * - Should be understandable to end users
     * - Should hint at the problem (missing field, validation error, etc.)
     * - Can include recovery suggestions for common errors
     * 
     * Examples:
     * - "An unexpected error occurred"
     * - "Some details are missing in the request"
     * - "Shortcode does not exist"
     */
    private String message;
}