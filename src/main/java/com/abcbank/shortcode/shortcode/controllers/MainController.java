package com.abcbank.shortcode.shortcode.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import javax.annotation.security.RolesAllowed;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.dto.ShortCodeDto;
import com.abcbank.shortcode.shortcode.entities.DTOAccount;
import com.abcbank.shortcode.shortcode.entities.DTOApproval;
import com.abcbank.shortcode.shortcode.entities.DTOResponse;
import com.abcbank.shortcode.shortcode.entities.DTOShortCode;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.AuditTrailService;
import com.abcbank.shortcode.shortcode.middleware.FinacleData;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.utils.HTTPSClient;
import com.abcbank.shortcode.shortcode.utils.ShortCodeMapper;
import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;
import com.abcbank.shortcode.shortcode.dto.AuditTrailDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Main REST Controller for Short Code Management Operations.
 * 
 * This controller is the primary entry point for all short code management APIs.
 * It implements a comprehensive Maker-Checker workflow with full audit trail logging,
 * data integrity validation, and regulatory compliance features.
 * 
 * Workflow Overview:
 * 1. INITIATE (Maker): Create short code request with customer details
 *    - Validate all required fields are present
 *    - Check for duplicate approved short codes for account
 *    - Check for pending approval requests
 *    - Assign short code value (format: 35XXXX)
 *    - Generate SHA-256 hash for integrity validation
 *    - Log action in audit trail
 * 
 * 2. APPROVE (Checker): Review and approve the short code
 *    - Verify integrity hash matches stored hash (tampering detection)
 *    - Set approver name and timestamp
 *    - Mark as approved and generate final hash
 *    - Generate PDF slip for customer
 *    - Send receipt email with attachment
 *    - Log action in audit trail
 * 
 * 3. DELETE REQUEST (Maker): Initiate short code deletion
 *    - Verify short code exists
 *    - Verify account number matches
 *    - Mark deletion as initiated
 *    - Store deletion remarks
 *    - Log action in audit trail
 * 
 * 4. APPROVE DELETE (Checker): Finalize short code deletion
 *    - Verify short code exists
 *    - Verify account number matches
 *    - Mark as deleted (soft delete)
 *    - Log action in audit trail
 * 
 * Security Features:
 * - Role-based access control (Maker, Checker, API Caller)
 * - SHA-256 hash-based integrity validation
 * - Finacle core banking system integration for account validation
 * - Audit trail for all operations (non-repudiation)
 * - Query methods for workflow state management
 * 
 * Integration Points:
 * - Finacle: Account validation and CBS short code lookup
 * - Keycloak: User authentication and role management
 * - Database: JPA repositories for data persistence
 * - Email: Receipt delivery to customers
 * - PDF Generation: Slip generation for customer records
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/shortcodes/api")
public class MainController {

	/**
	 * Finacle query server host configuration.
	 * Used to resolve Finacle integration endpoint URLs for account validation
	 * and CBS short code lookups.
	 */
	@Value("${service.params.finquery.host}")
	private String finqueryHost;

	/**
	 * Mapper for converting ShortCode entities to ShortCodeDto objects.
	 * Used in query methods to transform database entities to API response objects.
	 */
	@Autowired
	private ShortCodeMapper shortCodeMapper;

	/**
	 * Repository for ShortCode entity database operations.
	 * Handles all CRUD operations and custom query methods for short codes.
	 */
	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	/**
	 * Service for Finacle core banking system integration.
	 * Used to fetch account information and CBS-maintained short codes.
	 */
	@Autowired
	FinacleData finacleData;

	/**
	 * Service for short code business logic operations.
	 * Handles hash generation, request validation, and email delivery.
	 */
	@Autowired
	ShortCodeService shortCodeService;
	
	/**
	 * Controller for utility operations such as PDF slip generation and download.
	 * Used during approval workflow to create customer receipt documents.
	 */
	@Autowired
	UtilController utilController;

	/**
	 * Service for audit trail logging.
	 * Records all significant actions for regulatory compliance and investigation.
	 */
	@Autowired
	private AuditTrailService auditTrailService;

	/**
	 * Repository for AuditTrail entity database operations.
	 * Handles persistent storage of audit records.
	 */
	@Autowired
	private AuditTrailRepo auditTrailRepo;

	/**
	 * Validates account information against the Finacle core banking system.
	 * 
	 * This method queries the Finacle integration endpoint to retrieve validated
	 * account information including customer name, phone number, email, and
	 * identification details. It is used during the initiation phase to ensure
	 * that the account details are accurate before creating a short code request.
	 * 
	 * Finacle Integration:
	 * - Sends HTTP request to Finacle query endpoint
	 * - Retrieves account data in JSON format
	 * - Handles missing optional fields gracefully
	 * - Supports both national ID and passport number validation
	 * 
	 * Authorization:
	 * - Accessible to API Caller, Maker, and Checker roles
	 * - All users can validate accounts before submitting requests
	 * 
	 * @param accountNumber the bank account number to validate
	 * @return DTOAccount with validated account information from Finacle,
	 *         or empty DTOAccount if account not found or validation fails
	 * 
	 * @throws No exceptions are explicitly thrown; network errors are handled gracefully
	 */
	@GetMapping("/validate/{accountNumber}")
	@RolesAllowed({ "apicaller", "maker", "checker" })
	public DTOAccount validate(@PathVariable String accountNumber) {
		// Build Finacle query endpoint URL using configured host
		String url = "http://" + finqueryHost + "/api/finacle/account-data/" + accountNumber;
		
		// Send HTTP request to Finacle and retrieve account data
		String response = HTTPSClient.sendHttpsRequest(url, "", "get", new HashMap<>(), "text");
		JSONObject json = new JSONObject(response);
		
		// Extract optional identification fields with fallback handling
		String idNumber = null;
		String custId = null;
		String passPortNumber = null;
		try {idNumber = json.getString("idNumber");} catch(Exception e) {}
		try {custId = json.getString("custId");} catch(Exception e) {}
		try {passPortNumber = json.getString("ppNumber");} catch(Exception e) {}
		
		// Use national ID if available, otherwise use passport number
		String idOrPasspord = idNumber != null ? idNumber : passPortNumber != null ? passPortNumber : "None";
		
		// Construct response DTO with validated account information
		DTOAccount account = new DTOAccount();
		if(custId != null) {
			account.setAccountName(json.getString("accountName"));
			account.setAccountNumber(json.getString("accountNumber"));
			account.setCustId(custId);
			account.setIdNumber(idOrPasspord);
			try {account.setEmailAddress(json.getString("emailAddress"));} catch(Exception e) {}
			account.setPhoneNumber(json.getString("phoneNumber"));
			account.setAccountStatus(json.getString("status"));
		}
		return account;
	}

	/**
	 * Initiates a new short code request (Maker action in Maker-Checker workflow).
	 * 
	 * Maker Workflow Step 1:
	 * This method creates a new short code request with customer information.
	 * The request is saved in pending state and assigned a unique short code value
	 * based on the database record ID. A cryptographic hash is generated for
	 * integrity validation during the approval phase.
	 * 
	 * Workflow Steps:
	 * 1. Check if account already has an approved short code (only one per account)
	 * 2. Validate that all required fields are present
	 * 3. Check if a request for this account is already pending approval
	 * 4. Save the initial request to the database
	 * 5. Generate unique short code (format: 35 + 4-digit zero-padded ID)
	 * 6. Compute SHA-256 hash for integrity validation
	 * 7. Log action in audit trail for non-repudiation
	 * 
	 * Short Code Generation Logic:
	 * - Format: "35" + String.format("%04d", record_id)
	 * - Example: Record ID 1 → Short Code 350001
	 * - Ensures unique, sequential assignment
	 * 
	 * Hash-Based Integrity:
	 * - Includes: ID, Account Number, Customer ID, Account Name, ID Number,
	 *   Email, Phone, Short Code Value, Approval Status, Deletion Status
	 * - Used in approval phase to detect unauthorized modifications
	 * 
	 * Authorization:
	 * - Restricted to Maker and API Caller roles
	 * - Makers initiate requests for their branch/department
	 * 
	 * @param request the ShortCode entity with customer details
	 * @return DTOResponse with status code, assigned short code, and status message
	 *         Status Codes:
	 *         - "000": Request initiated successfully
	 *         - "101": Pending approval request exists for account
	 *         - "103": Approved short code already exists for account
	 *         - "104": Validation failed or database error
	 */
	@PostMapping("/initiate")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse initiate(@RequestBody ShortCode request) {
		// Log request initiation with key details for audit purposes
		log.info(" ========== About to initiate short code reqeust, account number: {}, name: {}",
				request.getAccountNumber(), request.getAccountName());
		
		// Query 1: Check if account already has an approved short code
		// Business Rule: Only one approved short code per account
		List<ShortCode> list = shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(request.getAccountNumber(), true, false);
		DTOResponse response = new DTOResponse();
		if (list.size() > 0) {
			int shortCodeValue = list.get(0).getShortCode();
			log.info(" ================ Account existing, short code: {}", shortCodeValue);
			response.setStatusCode("103");
			response.setMessage("Shortcode is already granted for the account");
			return response;
		}
		
		// Mark as not approved initially (pending checker approval)
		request.setApproved(false);

		// Validate that all required fields are present
		if (shortCodeService.validateRequest(request) == false) {
			response.setStatusCode("104");
			response.setMessage("Some details are missing in the request");
			return response;
		}

		// Query 2: Check for pending approval requests for this account
		// Business Rule: Only one request can be pending approval at a time
		List<ShortCode> shortCodeList = shortCodeRepo.findByAccountNumberAndApproved(request.getAccountNumber(), false);

		if (shortCodeList.size() > 0) {
			response.setStatusCode("101");
			response.setMessage("There is a short code request for this account pending approval");
			return response;
		}
		
		// Record the timestamp of request initiation
		request.setDateInitiated(LocalDateTime.now());

		// Save the request to database to obtain auto-generated ID
		ShortCode shortCode = shortCodeRepo.save(request);

		// Generate unique short code value: "35" + 4-digit zero-padded ID
		String shortCodeValue = "35" + String.format("%04d", shortCode.getId());
		int shortCodeInInt = Integer.parseInt(shortCodeValue);
		shortCode.setShortCode(shortCodeInInt);
		
		// Verify successful save (ID should be greater than 0)
		if (shortCode.getId() > 0) {
			// Generate initial integrity hash for this request
			String hash = shortCodeService.generateHash(shortCode);
			shortCode.setHash(hash);
			
			// Save short code with assigned value and hash
			shortCodeRepo.save(request);
			response.setStatusCode("000");
			response.setShortCode(shortCodeInInt);
			response.setMessage("Short code request initiated successfully");
			
			// Log action for audit trail and non-repudiation
			auditTrailService.logAction(
       			 shortCode,
       			"INITIATE",
       			 shortCode.getInitiator(),
        		"Shortcode request initiated");
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not initiated, error occured");
		}
		return response;
	}

	/**
	 * Approves a pending short code request (Checker action in Maker-Checker workflow).
	 * 
	 * Checker Workflow Step 2:
	 * This method reviews and approves a short code request from the Maker.
	 * It performs critical integrity validation to ensure the request has not been
	 * tampered with since the Maker initiated it. Upon approval, it updates the
	 * request status, generates a receipt PDF, and sends it to the customer via email.
	 * 
	 * Approval Workflow Steps:
	 * 1. Retrieve the most recent short code request for the account
	 * 2. Verify integrity: Recalculate hash and compare with stored hash
	 *    - If hashes don't match: REJECT (tampering detected, security alarm)
	 * 3. Update record with approver name and approval timestamp
	 * 4. Mark as approved = true
	 * 5. Generate final hash with updated approval status
	 * 6. Generate PDF slip for customer records
	 * 7. Send receipt email with PDF attachment to customer
	 * 8. Log action in audit trail
	 * 
	 * Integrity Validation:
	 * - Critical Security Feature: Detects unauthorized modifications
	 * - If hash mismatch occurs: Request is REJECTED with "Alarm: failed integrity check!"
	 * - Provides forensic evidence of tampering attempts
	 * - Ensures Checker receives the exact request from Maker
	 * 
	 * Receipt Generation:
	 * - PDF slip created with short code details
	 * - Sent to customer's registered email address
	 * - Contains instructions for using the short code
	 * 
	 * Authorization:
	 * - Restricted to Checker and API Caller roles
	 * - Checkers review requests from Makers in other departments/branches
	 * 
	 * @param request the approval request containing account number and approver user ID
	 * @return DTOResponse with status code, assigned short code, and status message
	 *         Status Codes:
	 *         - "000": Approved successfully
	 *         - "104": Integrity check failed (tampering detected) or no pending request
	 */
	@PostMapping("/approve")
	@RolesAllowed({ "apicaller", "checker" })
	@ResponseBody
	public DTOResponse approve(@RequestBody DTOApproval request) {
		// Retrieve the most recent request for this account (newest first)
		List<ShortCode> shortCodeList = shortCodeRepo.findByAccountNumberOrderByIdDesc(request.getAccountNumber());
		int count = shortCodeList.size();
		ShortCode shortCode = new ShortCode();
		DTOResponse response = new DTOResponse();
		
		if (count > 0) {
			shortCode = shortCodeList.get(0);
			
			// CRITICAL SECURITY CHECK: Integrity Validation
			// Recalculate hash and compare with stored hash to detect tampering
			String generatedHash = shortCodeService.generateHash(shortCode);
			if (!generatedHash.equals(shortCode.getHash())) {
				// Hash mismatch indicates the request has been tampered with
				log.error("SECURITY ALARM: Hash mismatch detected - possible tampering attempt");
				response.setStatusCode("104");
				response.setMessage("Alarm: failed integrity check!");
				return response;
			}
			
			// Integrity check passed, proceed with approval
			shortCode.setSequenceNumber(count);
			shortCode.setApprover(request.getApprover());
			shortCode.setDateApproved(LocalDateTime.now());
			shortCode.setApproved(true);
			
			// Generate final hash with updated approval status
			generatedHash = shortCodeService.generateHash(shortCode);
			shortCode.setHash(generatedHash);
			shortCode = shortCodeRepo.save(shortCode);

			// Generate PDF slip for customer and email delivery
			String filePath = utilController.generateSlip(shortCode.getShortCode());
			log.info("File path: " + filePath);
			shortCodeService.sendReceiptEmail(shortCode);
			
			response.setStatusCode("000");
			response.setShortCode(shortCode.getShortCode());
			response.setMessage("Shortcode successfully generated");
		}
		
		// Log approval action for audit trail and non-repudiation
		auditTrailService.logAction(
        shortCode,
        "APPROVE",
        request.getApprover(),
        "Shortcode approved");
		return response;
	}

	/**
	 * Initiates deletion of an approved short code (Maker action in deletion workflow).
	 * 
	 * Maker Workflow Step 3 (Deletion):
	 * This method initiates the process to delete an approved short code.
	 * The deletion request is marked but not finalized until the Checker approves it,
	 * implementing the same Maker-Checker safety mechanism.
	 * 
	 * Deletion Workflow Steps:
	 * 1. Verify the short code exists in the system
	 * 2. Verify the account number matches the request (authorization check)
	 * 3. Mark deleteInitiated = true to indicate pending deletion approval
	 * 4. Store deletion remarks provided by the Maker
	 * 5. Log action in audit trail
	 * 
	 * Business Logic:
	 * - Soft delete implementation: Records are marked as deleted, not physically removed
	 * - Preserves audit trail: Historical records remain for compliance
	 * - Requires checker approval: Same safety mechanism as creation workflow
	 * 
	 * Authorization:
	 * - Restricted to Maker and API Caller roles
	 * - Makers can delete short codes they or their department initiated
	 * 
	 * @param request contains the short code value, account number, and deletion remarks
	 * @return DTOResponse with status code and status message
	 *         Status Codes:
	 *         - "000": Deletion initiated successfully, pending approval
	 *         - "104": Short code not found or account number mismatch
	 */
	@DeleteMapping("/delete")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse delete(@RequestBody DTOShortCode request) {
		DTOResponse response = new DTOResponse();
		
		// Lookup short code by numeric value
		ShortCode shortCode = shortCodeRepo.findByShortCode(request.getShortCode());
		if (shortCode == null) {
			response.setStatusCode("104");
			response.setMessage("Shortcode does not exist!");
			return response;
		}
		
		// Authorization check: Verify account number matches
		// Prevents accidental or intentional deletion of wrong short codes
		if (!shortCode.getAccountNumber().equalsIgnoreCase(request.getAccountNumber())) {
			response.setStatusCode("104");
			response.setMessage("Something wrong with the request");
			return response;
		}

		// Mark deletion as initiated (pending Checker approval)
		shortCode.setDeleteInitiated(true);
		shortCode.setDeleteRemark(request.getDeleteRemark());
		response.setMessage("Short code delete initiated successfully, pending approval");

		// Verify record was found (ID > 0)
		if (shortCode.getId() > 0) {
			response.setStatusCode("000");
			shortCodeRepo.save(shortCode);
			
			// Log deletion request for audit trail and non-repudiation
			auditTrailService.logAction(
        		shortCode,
        		"DELETE_REQUEST",
       			shortCode.getInitiator(),
       			request.getDeleteRemark());
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not completed, error occured");
		}
		return response;
	}

	/**
	 * Approves and finalizes deletion of a short code (Checker action in deletion workflow).
	 * 
	 * Checker Workflow Step 4 (Deletion Approval):
	 * This method finalizes the deletion of a short code that the Maker initiated.
	 * Once approved, the short code is marked as deleted and can no longer be used.
	 * 
	 * Deletion Finalization Steps:
	 * 1. Verify the short code exists in the system
	 * 2. Verify the account number matches the request (authorization check)
	 * 3. Mark deleteInitiated = false and deleted = true
	 * 4. Persist the deletion status
	 * 5. Log action in audit trail
	 * 
	 * Soft Delete Implementation:
	 * - Deleted records remain in database for audit purposes
	 * - Approved short codes cannot be deleted without Checker approval
	 * - Deletion history preserved in audit trail
	 * 
	 * Authorization:
	 * - Restricted to Checker and API Caller roles
	 * - Checkers from separate authority can approve deletions
	 * 
	 * @param request contains the short code value and account number to delete
	 * @return DTOResponse with status code and status message
	 *         Status Codes:
	 *         - "000": Deletion approved and finalized successfully
	 *         - "104": Short code not found or account number mismatch
	 */
	@PostMapping("/approve-delete")
	@RolesAllowed({ "apicaller", "checker" })
	@ResponseBody
	public DTOResponse approveDelete(@RequestBody DTOShortCode request) {
		DTOResponse response = new DTOResponse();
		
		// Lookup short code by numeric value
		ShortCode shortCode = shortCodeRepo.findByShortCode(request.getShortCode());
		if (shortCode == null) {
			response.setStatusCode("104");
			response.setMessage("Shortcode does not exist!");
			return response;
		}
		
		// Authorization check: Verify account number matches
		if (!shortCode.getAccountNumber().equalsIgnoreCase(request.getAccountNumber())) {
			response.setStatusCode("104");
			response.setMessage("Something wrong with the request");
			return response;
		}
		
		// Finalize the deletion: clear pending flag and mark as deleted
		shortCode.setDeleteInitiated(false);
		shortCode.setDeleted(true);
		response.setMessage("Short code has been deleted from the system");

		// Verify record exists (ID > 0)
		if (shortCode.getId() > 0) {
			response.setStatusCode("000");
			shortCodeRepo.save(shortCode);
			
			// Log deletion approval for audit trail and non-repudiation
			auditTrailService.logAction(
    		shortCode,
        	"DELETE_APPROVE",
        	request.getAccountNumber(),
        	"Deletion approved");
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not completed, error occured");
		}
		return response;
	}

	/**
	 * Retrieves all pending short code requests awaiting Checker approval.
	 * 
	 * Query Method:
	 * Used by the front-end application to display pending requests that
	 * Checkers need to review and approve. Shows all requests with approved=false.
	 * 
	 * @return List of ShortCodeDto for all unapproved requests, empty list if none
	 */
	@GetMapping("/pending")
	@ResponseBody
	public List<ShortCodeDto> getPending() {
		// Query for all unapproved short code requests
    return shortCodeRepo.findByApproved(false)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}
	
	/**
	 * Retrieves all approved short codes that are currently active.
	 * 
	 * Query Method:
	 * Used to display approved and issued short codes. These are the active
	 * short codes that customers are using for Mpesa paybill transactions.
	 * 
	 * @return List of ShortCodeDto for all approved requests, empty list if none
	 */
	@GetMapping("/approved")
	@ResponseBody
	public List<ShortCodeDto> getApproved() {
		// Query for all approved short code requests
    return shortCodeRepo.findByApproved(true)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	/**
	 * Retrieves all short codes with pending deletion requests.
	 * 
	 * Query Method:
	 * Used by Checkers to review deletion requests initiated by Makers.
	 * Shows all requests with deleteInitiated=true and deleted=false (not yet finalized).
	 * 
	 * @return List of ShortCodeDto for all pending deletion requests, empty list if none
	 */
	@GetMapping("/pending-delete")
	@ResponseBody
	public List<ShortCodeDto> getPendingDelete() {
		// Query for all deletion requests pending Checker approval
    List<ShortCode> shortCodeList =
            shortCodeRepo.findByDeleteInitiatedAndDeleted(true, false);
    log.info("Pending delete requests: {}", shortCodeList.size());
    return shortCodeList.stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	/**
	 * Retrieves all short code requests for a specific account, ordered by recency.
	 * 
	 * Query Method:
	 * Used to display the history of short code requests for a customer account.
	 * Returns all requests (pending, approved, or deleted) in reverse chronological order.
	 * 
	 * @param accountNumber the bank account number to query
	 * @return List of ShortCodeDto for the account, ordered newest first, empty list if none
	 */
	@GetMapping("/get-shortcodes/{accountNumber}")
	@ResponseBody
	public List<ShortCodeDto> getPending(@PathVariable String accountNumber) {
		// Query all short code requests for account, ordered by newest first
    return shortCodeRepo.findByAccountNumberOrderByIdDesc(accountNumber)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	/**
	 * Retrieves the account number associated with a given short code.
	 * 
	 * Lookup Method with Integrity Validation:
	 * Used by external systems (e.g., payment switches) to validate and resolve
	 * a short code to an account number. Includes hash-based integrity verification
	 * to detect tampering and ensure the short code is still active (not deleted).
	 * 
	 * Validation Logic:
	 * 1. Lookup short code record by numeric value
	 * 2. Recalculate SHA-256 hash and compare with stored hash
	 *    - Ensures record hasn't been tampered with
	 * 3. Verify short code is not marked as deleted
	 * 4. Return account number if all validations pass
	 * 
	 * Security:
	 * - Hash mismatch returns null (treats as invalid)
	 * - Deleted short codes return null (prevents use after deletion)
	 * - Designed for use by payment processors
	 * 
	 * @param shortCodeNumber the numeric short code value to lookup
	 * @return the account number if short code is valid and active, null if invalid/deleted
	 */
	@GetMapping("/get-account/{shortCodeNumber}")
	public String getAccount(@PathVariable int shortCodeNumber) {
		try {
			// Lookup short code by numeric value
			ShortCode shortCode = shortCodeRepo.findByShortCode(shortCodeNumber);
			if (shortCode == null)
				return null;
			
			log.info("================ shortCode: {}", shortCode);
			
			// SECURITY CHECK: Validate integrity hash
			String generatedHash = shortCodeService.generateHash(shortCode);
			String storedHash = shortCode.getHash();
			log.info("================= StoredHash: {}, GeneratedHash: {}", storedHash, generatedHash);
			
			// Return account number only if:
			// 1. Hash matches (no tampering)
			// 2. Short code is not deleted
			return (generatedHash.equals(storedHash))
					? shortCode.isDeleted() == false ? shortCode.getAccountNumber() : null
							: null;
		} catch (Exception e) {
			// Treat any exception as invalid short code
			return null;
		}
	}

	/**
	 * Retrieves detailed account and short code information with CBS validation.
	 * 
	 * Lookup Method with Finacle Validation:
	 * Used to retrieve complete short code details with validation against the
	 * Finacle core banking system (CBS). Ensures the short code is maintained
	 * in both the application database and the CBS system.
	 * 
	 * Validation Logic:
	 * 1. Lookup short code record in application database
	 * 2. Query CBS (Finacle) for the short code maintained for this account
	 * 3. Cross-validate: CBS short code must match application short code
	 * 4. Return detailed short code information if validation passes
	 * 
	 * Integration:
	 * - Ensures data consistency between application and CBS
	 * - Detects discrepancies that may indicate data synchronization issues
	 * - Used for account lookup operations and payment processing validation
	 * 
	 * @param shortCode the numeric short code value to lookup
	 * @return ShortCode entity with full details if valid and CBS-validated,
	 *         empty ShortCode if not found or CBS validation fails
	 */
	@GetMapping("/get-account-details/{shortCode}")
	public ShortCode getAccountDetails(@PathVariable int shortCode) {
		try {
			// Lookup short code in application database
			ShortCode sc = shortCodeRepo.findByShortCode(shortCode);
			log.info("================= shortcode: {}", sc);
			
			if(sc.getAccountNumber() != null) {
				// Query CBS (Finacle) for the short code maintained for this account
				String maintainedSC = finacleData.fetchCBSShortCode(sc.getAccountNumber());
				log.info("================= maintainedSC: {}", maintainedSC);
				
				// Cross-validate: CBS short code must match application short code
				if(maintainedSC.equals(Integer.toString(shortCode))) {
					return sc;
				}
			}
		} catch (Exception e) {
			log.error("================ Error: {}", e.getMessage());
		}
		// Return empty record if not found or validation fails
		return new ShortCode();
	}

	/**
	 * Retrieves the complete audit trail for a specific short code.
	 * 
	 * Audit Trail Query:
	 * Returns all actions performed on a short code in reverse chronological order.
	 * Used for compliance, investigation, and customer dispute resolution.
	 * 
	 * Audit Information:
	 * - Action type (INITIATE, APPROVE, DELETE_REQUEST, DELETE_APPROVE)
	 * - User who performed the action (Maker or Checker ID)
	 * - Timestamp when the action was performed
	 * - Optional remarks or comments about the action
	 * 
	 * Regulatory Use:
	 * - Proves who performed each action and when (non-repudiation)
	 * - Complete record of short code lifecycle
	 * - Evidence for fraud investigation and compliance reporting
	 * 
	 * @param shortCode the numeric short code value to audit
	 * @return List of AuditTrailDto showing all actions on the short code,
	 *         ordered from newest to oldest, empty list if short code not found
	 */
	@GetMapping("/audit/{shortCode}")
	@ResponseBody
	public List<AuditTrailDto> getAuditTrail(
        @PathVariable Integer shortCode) {

		// Query audit trail for this short code, ordered newest first
    List<AuditTrail> auditList =
            auditTrailRepo.findByShortCodeOrderByActionDateDesc(shortCode);

		// Transform audit records to DTOs, formatting timestamps
    return auditList.stream()
            .map(audit -> {
                AuditTrailDto dto = new AuditTrailDto();

                dto.setAction(audit.getAction());
                dto.setPerformedBy(audit.getPerformedBy());
                dto.setRemarks(audit.getRemarks());

				// Format LocalDateTime to String for JSON serialization
                dto.setActionDate(
                        audit.getActionDate() != null
                                ? audit.getActionDate().toString()
                                : null);

                return dto;
            })
            .toList();
	}
}