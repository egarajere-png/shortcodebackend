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
import com.abcbank.shortcode.shortcode.dto.ShortCodeRegistryDto;
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

	
	@PostMapping("/initiate")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse initiate(@RequestBody ShortCode request) {
		// Log request initiation with key details for audit purposes
		log.info(" ========== About to initiate short code request, account number: {}, name: {}",
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

	
	@GetMapping("/pending")
	@ResponseBody
	public List<ShortCodeDto> getPending() {
		// Query for all unapproved short code requests
    return shortCodeRepo.findByApproved(false)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}
	
	
	@GetMapping("/approved")
	@ResponseBody
	public List<ShortCodeDto> getApproved() {
		// Query for all approved short code requests
    return shortCodeRepo.findByApproved(true)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	
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

	
	@GetMapping("/get-shortcodes/{accountNumber}")
	@ResponseBody
	public List<ShortCodeDto> getPending(@PathVariable String accountNumber) {
		// Query all short code requests for account, ordered by newest first
    return shortCodeRepo.findByAccountNumberOrderByIdDesc(accountNumber)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	
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

	@GetMapping("/short-code/{accountNumber}")
	public String getShortCode(
        @PathVariable String accountNumber) {

    List<ShortCode> shortCodes =
            shortCodeRepo.findByAccountNumberOrderByIdDesc(accountNumber);

    if (shortCodes != null && !shortCodes.isEmpty()) {

        return String.valueOf(
                shortCodes.get(0).getShortCode()
        );

    }

    return "";
	}

	@GetMapping("/registry")
	@ResponseBody
	public List<ShortCodeRegistryDto> getRegistry() {

    return shortCodeRepo.findAll()
            .stream()
            .map(sc -> {

                ShortCodeRegistryDto dto =
                        new ShortCodeRegistryDto();

                dto.setShortCode(sc.getShortCode());
                dto.setAccountNumber(sc.getAccountNumber());
                dto.setAccountName(sc.getAccountName());
                dto.setPhoneNumber(sc.getPhoneNumber());
                dto.setEmailAddress(sc.getEmailAddress());

                dto.setApproved(sc.isApproved());
                dto.setDeleted(sc.isDeleted());

                dto.setDateInitiated(
                        sc.getDateInitiated() != null
                                ? sc.getDateInitiated().toString()
                                : null);

                dto.setDateApproved(
                        sc.getDateApproved() != null
                                ? sc.getDateApproved().toString()
                                : null);

                return dto;
            })
            .toList();
}
}