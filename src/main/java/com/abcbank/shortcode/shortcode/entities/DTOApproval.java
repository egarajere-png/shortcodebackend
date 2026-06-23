package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Data Transfer Object for short code approval request.
 * 
 * This DTO is used in the approval workflow when a Checker approves a pending
 * short code request initiated by a Maker.
 * 
 * Approval Workflow:
 * 1. Maker initiates short code request via /initiate endpoint
 * 2. Checker reviews pending request
 * 3. Checker submits approval via POST /approve with this DTO
 * 4. System performs integrity check and generates final PDF
 * 
 * Usage:
 * - POST /approve endpoint: Request body contains approval details
 * - Identifies which account's short code to approve
 * - Records who (Checker) is approving the request
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
public class DTOApproval {
    /**
     * The account number of the short code request being approved.
     * Used to look up the most recent pending request for the account.
     */
    private String accountNumber;

    /**
     * The user ID of the Checker approving the request.
     * Recorded in the short code record and audit trail.
     * Provides non-repudiation: proves who approved the request.
     */
    private String approver;
}