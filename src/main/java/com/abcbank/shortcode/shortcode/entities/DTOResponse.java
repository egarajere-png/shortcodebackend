package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Data Transfer Object for generic API responses.
 * 
 * This DTO is used for successful operation responses throughout the API,
 * particularly for workflow operations (initiate, approve, delete, etc.).
 * It provides status information and short code value to the client.
 * 
 * Response Format:
 * - statusCode: Application-specific result code ("000" for success)
 * - shortCode: The numeric short code value if applicable
 * - message: Human-readable status message for user display
 * 
 * Usage:
 * - POST /initiate endpoint: Returns assigned short code on success
 * - POST /approve endpoint: Returns approved short code
 * - DELETE /delete endpoint: Returns confirmation message
 * - POST /approve-delete endpoint: Returns deletion confirmation
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
public class DTOResponse {
    /**
     * Application-specific status/result code.
     * Common values:
     * - "000": Successful operation
     * - "101": Pending request exists for account
     * - "103": Approved short code already exists
     * - "104": Validation error or operation failed
     */
    private String statusCode;

    /**
     * The numeric short code value assigned or returned.
     * Only populated for successful short code operations.
     */
    private int shortCode;

    /**
     * Human-readable message describing the operation result.
     * For success: "Short code request initiated successfully"
     * For failure: Description of what went wrong and recovery steps
     */
    private String message;
}
