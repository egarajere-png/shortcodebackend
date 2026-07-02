package com.abcbank.shortcode.shortcode.dto;

import lombok.Data;
import lombok.ToString;

/**
 * Data Transfer Object for Short Code API responses.
 * 
 * This DTO represents a short code with all associated metadata and workflow state.
 * It's used for API responses when querying short codes, including pending requests,
 * approved short codes, and deletion history.
 * 
 * Field Mapping:
 * Maps from ShortCode entity to DTO with timestamp conversion from LocalDateTime to String.
 * Transformation handled by ShortCodeMapper.toDto() method.
 * 
 * Usage:
 * - /pending endpoint: Lists all unapproved requests
 * - /approved endpoint: Lists all active short codes
 * - /pending-delete endpoint: Lists deletion requests awaiting approval
 * - /get-shortcodes/{accountNumber}: Lists account's short code history
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@ToString
@Data
public class ShortCodeDto {

    /** Unique database identifier for the short code record */
    private int id;

    /** User ID of the Maker who initiated the request */
    private String initiator;

    /** User ID of the Checker who approved the request */
    private String approver;

    /** The customer's bank account number */
    private String accountNumber;

    /** The customer's full name */
    private String accountName;

    /** The customer's phone number */
    private String phoneNumber;

    /** The customer's email address */
    private String emailAddress;

    /** The customer's national ID or passport number */
    private String idNumber;

    /** The customer's internal identifier in the core banking system */
    private String custId;

    /** Optional remark on the short code request */
    private String remark;

    /** Remark provided when deletion is requested */
    private String deleteRemark;

    /** The actual numeric short code value (e.g., 350001) */
    private int shortCode;

    /** Sequence number for ordering approved short codes */
    private int sequenceNumber;

    /** ISO 8601 timestamp string when the request was initiated */
    private String dateInitiated;

    /** ISO 8601 timestamp string when the request was approved */
    private String dateApproved;

    /** Flag indicating approval status (false = pending, true = approved) */
    private boolean approved = false;

    /** Flag indicating deletion is pending approval (false = not requested, true = requested) */
    private boolean deleteInitiated = false;

    /** Flag indicating the short code has been deleted (false = active, true = deleted) */
    private boolean deleted = false;

    private Integer preferredShortCode;

    private String status;
}