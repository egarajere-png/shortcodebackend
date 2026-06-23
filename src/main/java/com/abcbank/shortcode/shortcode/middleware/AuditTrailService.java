package com.abcbank.shortcode.shortcode.middleware;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;

/**
 * Service for comprehensive audit trail logging of short code operations.
 * 
 * This service implements non-repudiation and regulatory compliance by recording
 * all significant actions performed on short code records. Audit logs provide:
 * - Proof of who performed each action and when
 * - Complete action history for investigations and compliance
 * - Immutable records (append-only) for forensic purposes
 * 
 * Actions Logged:
 * - INITIATE: Request creation by Maker
 * - APPROVE: Approval by Checker with integrity validation
 * - DELETE_REQUEST: Deletion request by Maker
 * - DELETE_APPROVE: Deletion approval by Checker
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Service
public class AuditTrailService {

    /**
     * Repository for persisting audit trail records to the database.
     * Handles all CRUD operations for AuditTrail entities.
     */
    @Autowired
    private AuditTrailRepo auditTrailRepo;

    /**
     * Records an action performed on a short code in the audit trail.
     * 
     * This method creates an immutable audit record capturing who did what,
     * when they did it, and any additional context. It is called at critical
     * workflow points to ensure complete traceability of short code lifecycle.
     * 
     * Audit Record Contents:
     * - Short code ID and numeric value for quick reference
     * - Account number for transaction mapping
     * - Action type (INITIATE, APPROVE, DELETE_REQUEST, DELETE_APPROVE)
     * - Actor's user ID (Maker or Checker)
     * - Timestamp of the action
     * - Optional remarks providing additional context
     * 
     * Non-Repudiation:
     * The performedBy field establishes who performed the action, combined with
     * the actionDate timestamp and action type, this creates an audit trail that
     * cannot be disputed later.
     * 
     * Regulatory Compliance:
     * These records are essential for:
     * - Basel III compliance (operational risk management)
     * - Bank secrecy act compliance (transaction reporting)
     * - Internal fraud investigation and prevention
     * - Customer dispute resolution
     * 
     * @param shortCode the ShortCode entity that was acted upon
     * @param action the type of action performed (e.g., "INITIATE", "APPROVE")
     * @param performedBy the user ID of the person performing the action
     * @param remarks optional additional context or explanation
     */
    public void logAction(
            ShortCode shortCode,
            String action,
            String performedBy,
            String remarks) {

        // Create new audit trail entity to record this action
        AuditTrail audit = new AuditTrail();

        // Set references to the short code being acted upon
        audit.setShortCodeId(shortCode.getId());
        audit.setAccountNumber(shortCode.getAccountNumber());
        audit.setShortCode(shortCode.getShortCode());

        // Record the action details
        audit.setAction(action);
        audit.setPerformedBy(performedBy);
        audit.setRemarks(remarks);

        // Capture the current timestamp for the audit record
        audit.setActionDate(LocalDateTime.now());

        // Persist the audit trail record to the database
        auditTrailRepo.save(audit);
    }
}