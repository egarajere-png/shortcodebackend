package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.utils.Emailer;
import com.abcbank.shortcode.shortcode.utils.Hashing;

import lombok.extern.slf4j.Slf4j;

/**
 * Service component for short code business logic and operations.
 * 
 * This service encapsulates the core business logic for:
 * - Hash generation for data integrity validation
 * - Request validation to ensure completeness and correctness
 * - Receipt email delivery to customers after approval
 * 
 * The hash generation is critical for the system's security model, enabling
 * detection of unauthorized modifications to short code records. The hash
 * includes all essential attributes and is recalculated at key workflow stages.
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Slf4j
@Component
public class ShortCodeService {
	
	/**
	 * Email service for sending notifications and receipts.
	 * Used to send short code slips and confirmations to customers.
	 */
	@Autowired
	private Emailer emailer;

	/**
	 * Cryptographic hashing utility for SHA-256 hash computation.
	 * Used to generate checksums for short code data integrity validation.
	 */
	@Autowired
	private Hashing hashing;
	
	/**
	 * Sends a receipt email with the approved short code slip to the customer.
	 * 
	 * This method is invoked after a short code has been approved by the Checker.
	 * It constructs a professional email containing:
	 * - Personalized greeting with customer name
	 * - Information about the newly assigned short code
	 * - Instructions on how to use the short code for Mpesa paybill
	 * - Attached PDF slip containing the short code details
	 * 
	 * The email is sent from the official ABC Bank support mailbox to the
	 * customer's registered email address in the system.
	 * 
	 * @param shortCode the approved ShortCode entity containing customer details
	 *                  and short code information to include in the email
	 * @throws No exceptions are explicitly thrown; email errors are logged
	 */
	public void sendReceiptEmail(ShortCode shortCode) {
		
		log.info("Sending slip for eslip {}", shortCode.getShortCode());

		// Extract customer communication details from the short code record
		String emailAddress = shortCode.getEmailAddress();
		String customerName = shortCode.getAccountName();
		
		log.info("About to emailAddress: " + emailAddress);
		
		// Construct professional email message with standard banking tone
		String from = "ABC Bank Support<talk2us@abcthebank.com>";
		String subject = "ABC Bank - New Short-code " + shortCode.getShortCode();
		String body = "Dear " + customerName + ",\n\n" + 
					"Your ABC Bank-Mpesa short-code, " + shortCode.getShortCode() + " has been generated, kindly download the attached slip for your record." +
					"\nYou can share it so others can send you money with this short-code as the account for Mpesa paybill." +
					"\n\nABC Bank Team";
		
		// Send email with the generated PDF slip as attachment
		emailer.send(from, emailAddress.toLowerCase(), "", subject, body, "/tmp/" + shortCode.getShortCode() + ".pdf");
		log.info("Receipt has been sent on email");
	}
	
	/**
	 * Validates that a short code request contains all required information.
	 * 
	 * This method performs basic validation to ensure the request has the minimum
	 * required data before proceeding with database operations. It verifies that
	 * all critical customer identification and account information is present.
	 * 
	 * Required Fields Checked:
	 * - accountName: Customer's full name
	 * - accountNumber: Bank account number
	 * - idNumber: National ID or passport number
	 * - initiator: User ID of the Maker submitting the request
	 * - custId: Core banking system customer ID
	 * 
	 * @param shortCode the ShortCode request to validate
	 * @return true if all required fields are present and non-null, false otherwise
	 */
	public boolean validateRequest(ShortCode shortCode) {
		// Check that all required fields are populated
		if(shortCode.getAccountName() == null
				|| shortCode.getAccountNumber() == null
				|| shortCode.getIdNumber() == null
				|| shortCode.getInitiator() == null
				|| shortCode.getCustId() == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Generates a SHA-256 cryptographic hash of the short code record.
	 * 
	 * Hash-based Integrity Validation:
	 * This method creates a deterministic hash of all critical short code attributes.
	 * The hash is used to:
	 * 1. Detect unauthorized modifications to approved short code records
	 * 2. Validate data integrity during the approval workflow
	 * 3. Provide forensic evidence in case of suspected tampering
	 * 
	 * Hash includes (in pipe-delimited format):
	 * - Record ID
	 * - Account Number
	 * - Customer ID
	 * - Account Name
	 * - ID Number
	 * - Email Address
	 * - Phone Number
	 * - Short Code Value
	 * - Approval Status
	 * - Deletion Status
	 * 
	 * Workflow Integration:
	 * - Initial Hash: Generated during the initiation phase (before approval)
	 * - Final Hash: Recalculated during approval after all approver updates
	 * - Verification: Compared during account lookup to ensure data hasn't been tampered with
	 * 
	 * @param shortCode the ShortCode entity to hash
	 * @return the SHA-256 hash as a hexadecimal string
	 */
	public String generateHash(ShortCode shortCode) {

		// Build pipe-delimited string containing all attributes to be hashed
		String data = String.join("|",
				String.valueOf(shortCode.getId()),
				safe(shortCode.getAccountNumber()),
				safe(shortCode.getCustId()),
				safe(shortCode.getAccountName()),
				safe(shortCode.getIdNumber()),
				safe(shortCode.getEmailAddress()),
				safe(shortCode.getPhoneNumber()),
				String.valueOf(shortCode.getShortCode()),
				String.valueOf(shortCode.isApproved()),
				String.valueOf(shortCode.isDeleted())
		);

		log.info("Hash source data: {}", data);

		// Compute and return SHA-256 hash of the concatenated attributes
		return hashing.hash256(data);
	}

	/**
	 * Safely handles null or whitespace values in hash input.
	 * 
	 * This utility method ensures that null values are converted to empty strings
	 * and whitespace is trimmed, providing consistent hash inputs regardless of
	 * database field state. This prevents hash mismatches due to null handling
	 * differences between Java and database layers.
	 * 
	 * @param value the input string, potentially null or containing whitespace
	 * @return the trimmed string, or empty string if input was null
	 */
	private String safe(String value) {
		// Return empty string for null, otherwise return trimmed value
		return value == null ? "" : value.trim();
	}
}