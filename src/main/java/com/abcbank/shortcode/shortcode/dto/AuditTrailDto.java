package com.abcbank.shortcode.shortcode.dto;

import lombok.Data;

/**
 * Data Transfer Object for Audit Trail API responses.
 * 
 * This DTO represents a single audit trail entry for a short code operation.
 * It provides historical information about who did what and when for compliance
 * and investigation purposes.
 * 
 * Usage:
 * - /audit/{shortCode} endpoint: Returns list of audit entries for a short code
 * - Each entry represents one action (INITIATE, APPROVE, DELETE_REQUEST, DELETE_APPROVE)
 * 
 * Field Mapping:
 * Maps from AuditTrail entity to DTO with timestamp conversion from LocalDateTime to String.
 * Transformation handled by MainController.getAuditTrail() method.
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
public class AuditTrailDto {

    /**
     * The type of action performed on the short code.
     * Valid values: INITIATE, APPROVE, DELETE_REQUEST, DELETE_APPROVE
     */
    private String action;

    /**
     * The user ID of the person who performed the action.
     * Provides non-repudiation: proves who made the action.
     */
    private String performedBy;

    /**
     * Optional remarks or comments about the action.
     * Can contain explanation or context for the action.
     */
    private String remarks;

    /**
     * ISO 8601 timestamp string of when the action was performed.
     * Used for chronological ordering and compliance purposes.
     */
    private String actionDate;
}