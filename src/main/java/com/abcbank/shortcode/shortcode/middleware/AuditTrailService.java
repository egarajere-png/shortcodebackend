package com.abcbank.shortcode.shortcode.middleware;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;

/**
 * Service responsible for recording audit trail events.
 *
 * Every significant shortcode operation is stored for
 * traceability, compliance, and investigation purposes.
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
 * Records an audit trail entry.
 *
 * @param shortCode affected shortcode
 * @param action action performed
 * @param performedBy user performing the action
 * @param remarks additional information
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