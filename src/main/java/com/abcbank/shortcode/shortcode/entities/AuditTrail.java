package com.abcbank.shortcode.shortcode.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

/**
 * JPA Entity representing an audit trail record for short code operations.
 * 
 * This entity implements comprehensive audit logging for regulatory compliance and
 * investigation purposes. Every significant action on a short code is logged with
 * full context including the actor, action type, timestamp, and remarks.
 * 
 * Audit Trail Actions Tracked:
 * - INITIATE: Short code request created by Maker
 * - APPROVE: Short code approved by Checker (with final hash validation)
 * - DELETE_REQUEST: Deletion initiated by Maker
 * - DELETE_APPROVE: Deletion approved by Checker
 * 
 * Regulatory Compliance:
 * - Non-repudiation: Records prove who performed which action and when
 * - Immutability: Audit records are append-only and not modified
 * - Traceability: All changes to short code records are fully documented
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
@Entity
public class AuditTrail {

    /**
     * Unique database identifier for this audit trail record.
     * Auto-generated using database identity strategy.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the ShortCode entity ID that this audit record documents.
     * Allows direct mapping to the short code record being audited.
     */
    private Integer shortCodeId;

    /**
     * The customer's bank account number at the time of the action.
     * Preserved in audit record for historical reference even if the
     * short code record is later modified or deleted.
     */
    private String accountNumber;

    /**
     * The numeric short code value at the time of the action.
     * Used as a secondary identifier for audit trail retrieval and tracking.
     */
    private Integer shortCode;

    /**
     * The type of action performed on the short code.
     * 
     * Valid values:
     * - INITIATE: Initial request created by Maker
     * - APPROVE: Approved by Checker with integrity validation
     * - DELETE_REQUEST: Deletion requested by Maker
     * - DELETE_APPROVE: Deletion approved by Checker
     */
    private String action;

    /**
     * User ID of the person who performed the action.
     * Provides non-repudiation: proves who made each change.
     */
    private String performedBy;

    /**
     * Optional remarks or comments about the action.
     * Can contain additional context, error messages, or explanation of the action.
     */
    private String remarks;

    /**
     * Timestamp of when the action was performed.
     * Set automatically using the system clock when the action is recorded.
     * Used for chronological ordering and compliance purposes.
     */
    private LocalDateTime actionDate;
}