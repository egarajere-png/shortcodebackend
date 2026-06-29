package com.abcbank.shortcode.shortcode.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;

import org.springframework.data.domain.Pageable;
/**
 * Spring Data JPA Repository for AuditTrail entity database operations.
 * 
 * This repository provides CRUD operations and custom query methods for the
 * AuditTrail entity. It encapsulates all database interactions for audit trail
 * records used in compliance, investigation, and non-repudiation purposes.
 * 
 * Query Methods:
 * - findByShortCodeIdOrderByActionDateDesc: Find audit entries for a record ID
 * - findByAccountNumberOrderByActionDateDesc: Find audit entries for an account
 * - findByShortCodeOrderByActionDateDesc: Find audit entries for a short code value
 * 
 * Audit Trail Records:
 * - INITIATE: Request creation by Maker
 * - APPROVE: Approval by Checker with hash validation
 * - DELETE_REQUEST: Deletion requested by Maker
 * - DELETE_APPROVE: Deletion approved by Checker
 * 
 * Regulatory Compliance:
 * - Non-repudiation: Records prove who did what and when
 * - Immutability: Audit records are append-only, never modified or deleted
 * - Traceability: Complete history of all short code operations
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
public interface AuditTrailRepo extends JpaRepository<AuditTrail, Long> {

    /**
     * Finds all audit trail entries for a specific short code record, ordered by date.
     * 
     * Used when querying audit history by the database record ID.
     * Returns entries in reverse chronological order (newest first).
     * 
     * @param shortCodeId the database ID of the ShortCode record
     * @return List of AuditTrail entries ordered by action date descending
     */
    List<AuditTrail> findByShortCodeIdOrderByActionDateDesc(Integer shortCodeId);

    /**
     * Finds all audit trail entries for a specific account number, ordered by date.
     * 
     * Used to retrieve audit history for all short codes associated with an account.
     * Returns entries in reverse chronological order (newest first).
     * 
     * @param accountNumber the customer's bank account number
     * @return List of AuditTrail entries ordered by action date descending
     */
    List<AuditTrail> findByAccountNumberOrderByActionDateDesc(String accountNumber);

    /**
     * Finds all audit trail entries for a specific short code value, ordered by date.
     * 
     * Most commonly used method. Queries by the customer-visible short code number
     * (e.g., 350001) to retrieve the complete history of actions on that short code.
     * Returns entries in reverse chronological order (newest first).
     * 
     * Used by the /audit/{shortCode} endpoint to return audit history to clients.
     * 
     * @param shortCode the numeric short code value (e.g., 350001)
     * @return List of AuditTrail entries ordered by action date descending
     */
    List<AuditTrail> findByShortCodeOrderByActionDateDesc(Integer shortCode);
    List<AuditTrail> findAllByOrderByActionDateDesc(Pageable pageable);
}