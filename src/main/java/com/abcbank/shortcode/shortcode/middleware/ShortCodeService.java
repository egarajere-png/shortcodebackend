package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.utils.Emailer;
import com.abcbank.shortcode.shortcode.utils.Hashing;

import lombok.extern.slf4j.Slf4j;

/**
 * Service containing core shortcode business logic.
 *
 * Handles request validation, hash generation,
 * and customer notification.
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
 * Sends the approved shortcode receipt
 * to the customer's email address.
 *
 * @param shortCode approved shortcode.
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
 * Validates that all mandatory shortcode
 * request fields have been provided.
 *
 * @param shortCode request to validate
 * @return true if valid, otherwise false.
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
 * Generates a SHA-256 hash for a shortcode record.
 *
 * The hash is used to verify data integrity and detect
 * unauthorized modifications.
 *
 * @param shortCode shortcode record
 * @return SHA-256 hash.
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
 * Converts null values to empty strings
 * before hash generation.
 */
	private String safe(String value) {
		// Return empty string for null, otherwise return trimmed value
		return value == null ? "" : value.trim();
	}
}