package com.abcbank.shortcode.shortcode.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;
import lombok.ToString;

/**
 * Entity representing a customer shortcode.
 * Stores customer details, shortcode information,
 * approval status, deletion status, audit timestamps,
 * and integrity hash used throughout the Maker-Checker workflow.
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
	 */
	private String initiator;

	/**
	 * User ID of the employee who approved this short code request (Checker role).
	 */
	private String approver;

	/**
	 * The customer's bank account number associated with this short code.
	 */
	private String accountNumber;

	/**
	 * The customer's full name as it appears in the core banking system.
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
	 */
	private String idNumber;

	/**
	 * The customer's internal identification number in the core banking system.
	 */
	private String custId;

	/**
	 * Optional remark or comment on the short code request.
	 */
	private String remark;

	/**
	 * Remark provided when deletion is requested by the Maker.
	 */
	@Column(columnDefinition = "varchar(255) default ''")
	private String deleteRemark;

	/**
	 * The actual short code numeric value assigned to this request.
	 */
	private int shortCode;
	
	/**
	 * Optional customer preferred shortcode.
	 */
	@Column(nullable = true, unique = true)
	private Integer preferredShortCode;

	/**
	 * Sequence number used to track the order of approved short codes for a customer.
	 */
	private int sequenceNumber;

	/**
	 * Timestamp when the short code request was first created by the Maker.
	 */
	private LocalDateTime dateInitiated;

	/**
	 * Timestamp when the short code was approved by the Checker.
	 */
	private LocalDateTime dateApproved;

	/**
	 * Flag indicating whether the short code has been approved by a Checker.
	 */
	private boolean approved = false;

	/**
	 * Flag indicating that deletion has been initiated by the Maker.
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean deleteInitiated = false;

	/**
	 * Flag indicating that the short code has been permanently deleted from the system.
	 * 
	 */
	@Column(columnDefinition = "boolean default false")
	private boolean deleted = false;


	private LocalDateTime dateDeleted;

	/**
	 * SHA-256 hash of critical record attributes for integrity validation.
	 */
	
	private String hash;
}
