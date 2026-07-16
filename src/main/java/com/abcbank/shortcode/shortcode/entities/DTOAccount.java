package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * DTO representing customer account information retrieved
 * from the core banking system during account validation.
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
