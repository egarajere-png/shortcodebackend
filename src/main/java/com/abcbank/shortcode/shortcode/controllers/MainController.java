package com.abcbank.shortcode.shortcode.controllers;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.HashMap;
import java.util.List;


import jakarta.annotation.security.RolesAllowed;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

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
import com.abcbank.shortcode.shortcode.services.ExportService;
// import com.abcbank.shortcode.shortcode.utils.HTTPSClient;
import com.abcbank.shortcode.shortcode.utils.ShortCodeMapper;
import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;
import com.abcbank.shortcode.shortcode.dto.AuditTrailDto;
// import java.util.Set;

import java.io.ByteArrayInputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.abcbank.shortcode.shortcode.services.ExportService;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/shortcodes/api")
public class MainController {

	// This is for the reserved numbers that cannot be used as a short code. 
	// These are hardcoded to prevent conflicts with special codes or test values.
	private static final java.util.Set<Integer> RESERVED_SHORTCODES = java.util.Set.of(
        0,
        111111,
        123456,
        222222,
        333333,
        444444,
        555555,
        666666,
        777777,
        888888,
        999999
);
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

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ExportService exportService;


	@GetMapping("/validate/{accountNumber}")
	@RolesAllowed({"apicaller","maker","checker"})
	public DTOAccount validate(@PathVariable String accountNumber) {

    JSONObject json = finacleData.fetchAccount(accountNumber);

    String idNumber = json.optString("idNumber", null);
    String custId = json.optString("custId", null);
    String passport = json.optString("ppNumber", null);

    String id = idNumber != null
            ? idNumber
            : passport != null
                ? passport
                : "None";

    DTOAccount account = new DTOAccount();

    if (custId != null) {

        account.setAccountName(json.getString("accountName"));
        account.setAccountNumber(json.getString("accountNumber"));
        account.setCustId(custId);
        account.setIdNumber(id);
        account.setEmailAddress(json.optString("emailAddress", ""));
        account.setPhoneNumber(json.getString("phoneNumber"));
        account.setAccountStatus(json.getString("status"));
    }

    return account;
}

	
	@PostMapping("/initiate")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse initiate(@RequestBody ShortCode request) {

    log.info(" ========== About to initiate short code request, account number: {}, name: {}",
            request.getAccountNumber(), request.getAccountName());

    DTOResponse response = new DTOResponse();

    // Check if account already has an approved shortcode
    List<ShortCode> list =
            shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
                    request.getAccountNumber(),
                    true,
                    false);

    if (!list.isEmpty()) {

        response.setStatusCode("103");
        response.setMessage("Shortcode is already granted for the account");
        return response;
    }

    request.setApproved(false);

    if (!shortCodeService.validateRequest(request)) {

        response.setStatusCode("104");
        response.setMessage("Some details are missing in the request");
        return response;
    }

    List<ShortCode> pending =
            shortCodeRepo.findByAccountNumberAndApproved(
                    request.getAccountNumber(),
                    false);

    if (!pending.isEmpty()) {

        response.setStatusCode("101");
        response.setMessage("There is a shortcode request pending approval.");
        return response;
    }

    /*
     * ===================================================
     * Validate preferred shortcode BEFORE saving
     * ===================================================
     */

    if (request.getShortCode() > 0) {

        int preferred = request.getShortCode();

        if (preferred < 100000 || preferred > 999999) {

            response.setStatusCode("104");
            response.setMessage("Preferred shortcode must be exactly 6 digits.");
            return response;
        }

        if (RESERVED_SHORTCODES.contains(preferred)) {

            response.setStatusCode("104");
            response.setMessage("Selected shortcode is reserved.");
            return response;
        }

        ShortCode existing = shortCodeRepo.findByShortCode(preferred);

			if (existing != null && !existing.isDeleted()) {

    		response.setStatusCode("104");
    		response.setMessage(
       		 existing.isApproved()
           		 ? "Selected shortcode is already allocated."
           		 : "Selected shortcode has already been requested and is awaiting approval."
    		);

    return response;
}

    }

    request.setDateInitiated(LocalDateTime.now());

    /*
     * Save AFTER all validations
     */
    ShortCode shortCode = shortCodeRepo.save(request);

    int finalShortCode;

    if (request.getShortCode() > 0) {

        finalShortCode = request.getShortCode();

    } else {

        String generated =
                "35" + String.format("%04d", shortCode.getId());

        finalShortCode = Integer.parseInt(generated);

    }

    shortCode.setShortCode(finalShortCode);

    String hash = shortCodeService.generateHash(shortCode);
    shortCode.setHash(hash);

    shortCodeRepo.save(shortCode);

    response.setStatusCode("000");
    response.setShortCode(finalShortCode);
    response.setMessage("Shortcode request initiated successfully");

    auditTrailService.logAction(
            shortCode,
            "INITIATE",
            shortCode.getInitiator(),
            "Shortcode request initiated");

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
	// @RolesAllowed({ "apicaller", "checker" })
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
	// @RolesAllowed({"checker", "apicaller"})
	@ResponseBody
	public List<ShortCodeDto> getPending() {
		// Query for all unapproved short code requests
    return shortCodeRepo.findByApproved(false)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}
	
	
	@GetMapping("/approved")
	// @RolesAllowed({"checker", "apicaller"})
	@ResponseBody
	public List<ShortCodeDto> getApproved() {
		// Query for all approved short code requests
    return shortCodeRepo.findByApproved(true)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	
	@GetMapping("/pending-delete")
	// @RolesAllowed({"checker", "apicaller"})
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
	// @RolesAllowed({"maker","checker","apicaller"})
	@ResponseBody
	public List<ShortCodeDto> getPending(@PathVariable String accountNumber) {
		// Query all short code requests for account, ordered by newest first
    return shortCodeRepo.findByAccountNumberOrderByIdDesc(accountNumber)
            .stream()
            .map(shortCodeMapper::toDto)
            .toList();
	}

	
	@GetMapping("/get-account/{shortCodeNumber}")
	// @RolesAllowed({"checker","apicaller"})
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
// @RolesAllowed({"maker","checker","apicaller"})
public ShortCode getAccountDetails(@PathVariable int shortCode) {

    try {

        // Lookup shortcode in application database
        ShortCode sc = shortCodeRepo.findByShortCode(shortCode);

        log.info("================= shortcode: {}", sc);

        if (sc == null) {
            return new ShortCode();
        }

        // Validate record integrity
        String generatedHash = shortCodeService.generateHash(sc);

        if (!generatedHash.equals(sc.getHash())) {
            log.error("Hash mismatch detected for shortcode {}", shortCode);
            return new ShortCode();
        }

        // Return the database record
        return sc;

    } catch (Exception e) {
        log.error("Error retrieving shortcode details", e);
        return new ShortCode();
    }
}

	@GetMapping("/audit/{shortCode}")
	// @RolesAllowed({"checker","apicaller"})
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
	// @RolesAllowed({"maker","checker","apicaller"})
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
	// @RolesAllowed({"checker","apicaller"})
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
				dto.setDeleteInitiated(sc.isDeleteInitiated());

                dto.setDateInitiated(
                        sc.getDateInitiated() != null
                                ? sc.getDateInitiated().toString()
                                : null);

                dto.setDateApproved(
                        sc.getDateApproved() != null
                                ? sc.getDateApproved().toString()
                                : null);
						
			
					if (sc.isDeleted()) {
    				dto.setStatus("Deleted");
					} 
					else if (sc.isDeleteInitiated()) {
   						 dto.setStatus("Pending Deletion");
} else if (!sc.isApproved()) {
    dto.setStatus("Pending Approval");
} else {
    dto.setStatus("Active");
}

                return dto;
            })
            .toList();
}

@GetMapping("/check-shortcode/{shortCode}")
// @RolesAllowed({"maker","apicaller"})
@ResponseBody
public DTOResponse checkShortCode(
        @PathVariable int shortCode) {

    DTOResponse response = new DTOResponse();

    // Must be exactly 6 digits
    if (shortCode < 100000 || shortCode > 999999) {

        response.setStatusCode("104");
        response.setAvailable(false);
        response.setMessage("Shortcode must be exactly 6 digits.");

        return response;
    }

    // Reserved
    if (RESERVED_SHORTCODES.contains(shortCode)) {

        response.setStatusCode("102");
        response.setAvailable(false);
        response.setMessage("Reserved shortcode.");

        return response;
    }

    // Check whether the shortcode already exists
ShortCode existing = shortCodeRepo.findByShortCode(shortCode);

if (existing != null) {

    /*
     * If the shortcode is already APPROVED and not deleted,
     * then it is genuinely taken.
     */
    if (existing.isApproved() && !existing.isDeleted()) {

        response.setStatusCode("101");
        response.setAvailable(false);
        response.setMessage("Shortcode already taken.");

        return response;
    }

    /*
     * If it is still pending approval,
     * allow the checker to continue.
     */
    if (!existing.isApproved()) {

        response.setStatusCode("000");
        response.setAvailable(true);
        response.setMessage("Pending request can be approved.");

        return response;
    }

    /*
     * Deleted shortcodes become reusable.
     */
    if (existing.isDeleted()) {

        response.setStatusCode("000");
        response.setAvailable(true);
        response.setMessage("Previously deleted shortcode.");

        return response;
    }
}

    response.setStatusCode("000");
    response.setAvailable(true);
    response.setMessage("Shortcode available.");

    return response;
}

@GetMapping("/registry/export/excel")
public ResponseEntity<InputStreamResource> exportRegistryExcel() {

    ByteArrayInputStream in = exportService.exportRegistryToExcel();

    HttpHeaders headers = new HttpHeaders();

    headers.add(
            "Content-Disposition",
            "attachment; filename=Shortcode_Registry.xlsx");

    return ResponseEntity.ok()
            .headers(headers)
            .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(new InputStreamResource(in));
}
}