package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Data Transfer Object for short code deletion request.
 * 
 * This DTO is used in the deletion workflow when a Maker requests deletion
 * of an approved short code.
 * 
 * Deletion Workflow:
 * 1. Maker initiates deletion via DELETE /delete with this DTO
 * 2. System marks deleteInitiated = true, stores deletion remarks
 * 3. Checker reviews pending deletion request
 * 4. Checker approves deletion via POST /approve-delete
 * 5. Short code is marked as deleted
 * 
 * Usage:
 * - DELETE /delete endpoint: Request body contains account and short code
 * - POST /approve-delete endpoint: Checker approval of deletion request
 * - Captures reason/remarks for deleting the short code
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
public class DTOShortCode {
    /**
     * The customer's bank account number.
     * Used to verify the account owns the short code being deleted.
     * Acts as authorization check to prevent accidental/malicious deletions.
     */
    private String accountNumber;

    /**
     * Optional remark explaining why the short code is being deleted.
     * Examples: "Lost device", "Account closed", "Customer request"
     * Recorded in the short code record and audit trail.
     */
    private String deleteRemark;

    /**
     * The numeric short code value to delete.
     * Used to look up the short code record in the database.
     */
    private int shortCode;
}