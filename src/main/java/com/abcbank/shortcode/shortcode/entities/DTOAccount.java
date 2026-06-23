package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Data Transfer Object for account information from Finacle core banking system.
 * 
 * This DTO represents account details retrieved from the Finacle query endpoint
 * or mock Finacle controller. It's returned by the account validation endpoint
 * and used by the UI for customer verification during short code creation.
 * 
 * Usage:
 * - /validate/{accountNumber} endpoint: Returns account data for verification
 * - Contains customer identification and contact information
 * - Used by frontend to display customer details before short code creation
 * 
 * Data Source:
 * - Finacle core banking system (production)
 * - Mock Finacle controller (development/testing)
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
public class DTOAccount {

    /** The bank account number (unique identifier) */
    private String accountNumber;

    /** The customer's full name as registered in core banking */
    private String accountName;

    /** Internal customer identifier in Finacle */
    private String custId;

    /** National identification number or passport number */
    private String idNumber;

    /** Customer's registered phone number */
    private String phoneNumber;

    /** Customer's registered email address */
    private String emailAddress;

    /** Account status (ACTIVE, DORMANT, CLOSED, SUSPENDED, etc.) */
    private String accountStatus;
}
