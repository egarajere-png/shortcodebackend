package com.abcbank.shortcode.shortcode.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for REST API error responses.
 * 
 * This class provides centralized exception handling for the entire application.
 * It catches unhandled exceptions and converts them to consistent, user-friendly
 * API error responses.
 * 
 * Exception Handling Strategy:
 * - Logs full exception stack traces for debugging and investigation
 * - Returns generic error message to client (avoids exposing system details)
 * - HTTP status: 500 Internal Server Error
 * - Consistent response format: ApiErrorResponse with status code and message
 * 
 * Extensibility:
 * Can be extended with additional @ExceptionHandler methods to handle
 * specific exception types:
 * - ValidationException → 400 Bad Request
 * - EntityNotFoundException → 404 Not Found
 * - UnauthorizedException → 401 Unauthorized
 * - AccessDeniedException → 403 Forbidden
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles uncaught exceptions and returns a standardized error response.
     * 
     * Error Handling:
     * 1. Logs full exception with stack trace for investigation
     * 2. Creates generic error response (no sensitive details exposed)
     * 3. Returns HTTP 500 status with error response body
     * 4. Prevents exception stack traces from being exposed to clients
     * 
     * Security Considerations:
     * - Generic error messages prevent information disclosure
     * - Stack traces logged server-side for debugging
     * - Clients receive friendly but non-technical error messages
     * - Supports production deployments without exposing internals
     * 
     * @param ex the unhandled exception thrown during request processing
     * @return ResponseEntity with HTTP 500 status and standardized error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex) {

        // Log full exception for debugging and investigation
        log.error("Unhandled exception occurred", ex);

        // Create standardized error response with generic message
        ApiErrorResponse error = new ApiErrorResponse(
                "500",
                "An unexpected error occurred"
        );

        // Return HTTP 500 error with standardized response body
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}