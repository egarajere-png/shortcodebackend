package com.abcbank.shortcode.shortcode.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

/**
 * JPA Entity representing a Short Code (USSD code) record in the system.
 * 
 * A short code is a numeric identifier used in Mpesa paybill transactions that allows
 * customers to send money using a shortened code instead of an account number.
 * 
 * This entity implements a Maker-Checker workflow:
 * - Initiator (Maker): Creates a short code request with customer information
 * - Approver (Checker): Reviews and approves the short code request
 * - System: Performs integrity checks via SHA-256 hash validation
 * 
 * Lifecycle State Transitions:
 * 1. INITIATED: Created by Maker, approved=false, deleteInitiated=false
 * 2. APPROVED: Approved by Checker, approved=true, deleteInitiated=false
 * 3. DELETE_REQUESTED: Deletion initiated by Maker, approved=true, deleteInitiated=true
 * 4. DELETED: Deletion approved by Checker, deleted=true, deleteInitiated=false
 * 
 * Data Integrity:
 * - Hash field stores SHA-256 checksum of critical record attributes
 * - Hash is recalculated at initiation and approval stages
 * - Hash verification prevents unauthorized modifications to approved records
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@ToString
@Data
@Entity
public class ShortCode {

	/**
	 * Unique database identifier for this short code record.
	 * Auto-generated using database identity strategy.
	 */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

	/**
	 * User ID of the employee who initiated this short code request (Maker role).
	 * Required field that must be provided during request initiation.
	 */
	private String initiator;

	/**
	 * User ID of the employee who approved this short code request (Checker role).
	 * Set during the approval phase of the Maker-Checker workflow.
	 */
	private String approver;

	/**
	 * The customer's bank account number associated with this short code.
	 * Must be validated against the core banking system (Finacle) for accuracy.
	 */
	private String accountNumber;

	/**
	 * The customer's full name as it appears in the core banking system.
	 * Used for identification and communication purposes.
	 */
	private String accountName;

	/**
	 * The customer's phone number, used for communication and verification.
	 */
	private String phoneNumber;

	/**
	 * The customer's email address, used for sending short code slip and notifications.
	 */
	private String emailAddress;

	/**
	 * The customer's national identification number or passport number.
	 * Required for KYC compliance and verification.
	 */
	private String idNumber;

	/**
	 * The customer's internal identification number in the core banking system.
	 * Required for integration with Finacle.
	 */
	private String custId;

	/**
	 * Optional remark or comment on the short code request.
	 * Can contain additional context for the Maker or Checker.
	 */
	private String remark;

	/**
	 * Remark provided when deletion is requested by the Maker.
	 * Explains the reason for short code deletion.
	 */
	@Column(columnDefinition = "varchar(255) default ''")
	private String deleteRemark;

	/**
	 * The actual short code numeric value assigned to this request.
	 * Format: 3XXXXX (e.g., 350001 for ID 1)
	 * Generated from the record ID: "35" + zero-padded 4-digit ID
	 */
	private int shortCode;

	/**
	 * Sequence number used to track the order of approved short codes for a customer.
	 * Set during the approval phase.
	 */
	private int sequenceNumber;

	/**
	 * Timestamp when the short code request was first created by the Maker.
	 * Set during the initiation phase.
	 */
	private LocalDateTime dateInitiated;

	/**
	 * Timestamp when the short code was approved by the Checker.
	 * Set during the approval phase; null if not yet approved.
	 */
	private LocalDateTime dateApproved;

	/**
	 * Flag indicating whether the short code has been approved by a Checker.
	 * false = pending approval, true = approved
	 * Default: false
	 */
	private boolean approved = false;

	/**
	 * Flag indicating that deletion has been initiated by the Maker.
	 * Part of the deletion workflow: true = deletion requested, false = not requested
	 * Default: false
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean deleteInitiated = false;

	/**
	 * Flag indicating that the short code has been permanently deleted from the system.
	 * Only set to true after Checker approval of deletion.
	 * Default: false
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean deleted = false;

	/**
	 * SHA-256 hash of critical record attributes for integrity validation.
	 * 
	 * Hash includes: id, accountNumber, custId, accountName, idNumber, 
	 * emailAddress, phoneNumber, shortCode, approved, deleted
	 * 
	 * Used to detect unauthorized modifications to approved records.
	 * Recalculated and verified at critical workflow stages.
	 */
	private String hash;
}
