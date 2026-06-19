package com.abcbank.shortcode.shortcode.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.utils.SlipGenerator;

import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for utility operations related to short code processing.
 * 
 * This controller handles operations that support the short code workflow but are not
 * part of the core Maker-Checker logic, including PDF slip generation and download.
 * 
 * Key Responsibilities:
 * - Generate PDF slips containing short code details for customer records
 * - Provide file download endpoints for customers to retrieve their slips
 * - Manage temporary file storage for generated slips
 * 
 * Workflow Integration:
 * - Slip generation is triggered during short code approval
 * - Generated slips are cached locally to avoid regeneration
 * - Slips are attached to approval notification emails
 * - Customers can download slips later using this endpoint
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Slf4j
@RestController
@Service
@RequestMapping("/shortcodes")
public class UtilController {

	/**
	 * Finacle query server host configuration.
	 * Configuration parameter for Finacle integration endpoints.
	 */
	@Value("${service.params.finquery.host}")
	private String finqueryHost;
	
	/**
	 * Repository for ShortCode entity database operations.
	 */
	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	/**
	 * Service for short code business logic and operations.
	 */
	@Autowired
	ShortCodeService shortCodeService;

	/**
	 * Main method placeholder for testing and demonstration.
	 * This is a utility method not used in production workflow.
	 */
	public static void main(String[] args) {
	    ShortCode shortie = new ShortCode();
		shortie.setAccountName("Samuel Waithaka");
		shortie.setAccountNumber("001190001000062");
		shortie.setCustId("001133300");
		shortie.setEmailAddress("samuel.waithaka@abcthebank.com");
		shortie.setId(12);
		shortie.setShortCode(3500023);
		System.out.println(shortie.hashCode());	
	}

	/**
	 * Downloads the PDF slip for a given short code.
	 * 
	 * File Download Endpoint:
	 * Allows customers and internal staff to download the PDF slip containing
	 * short code details. The slip includes the short code value, account number,
	 * customer name, and instructions for use in Mpesa transactions.
	 * 
	 * HTTP Response Headers:
	 * - Content-Disposition: attachment to trigger browser download
	 * - Cache-Control: no-cache, no-store to prevent caching
	 * - Content-Type: application/octet-stream for binary PDF file
	 * 
	 * File Handling:
	 * - Generates slip if file doesn't exist (lazy generation)
	 * - Returns pre-generated file if it exists (cached)
	 * - File stored in /tmp/ directory with naming: {shortCode}.pdf
	 * 
	 * @param shortCode the numeric short code value
	 * @return ResponseEntity with PDF file as attachment, ready for download
	 *         Returns null on error (should return proper error response)
	 * 
	 * @throws IOException if file operations fail
	 */
	@GetMapping("/print/{shortCode}")
	public ResponseEntity<Resource> downloadSlip(@PathVariable int shortCode) throws IOException {
		try {
			// Build PDF filename from short code value
			String fileName = shortCode + ".pdf";
			// Generate slip file and retrieve file path
			String filePath = generateSlip(shortCode);
			File file = new File(filePath);

			// Prepare HTTP headers for file download
			HttpHeaders header = new HttpHeaders();
			// Instruct browser to download as attachment (not display inline)
			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
			// Prevent browser and proxy caching of the response
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			// Read file from disk into memory
			Path path = Paths.get(file.getAbsolutePath());
			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

			// Return file with appropriate headers and content type
			return ResponseEntity.ok()
					.headers(header)
					.contentLength(file.length())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(resource);
		} catch(Exception e) {
			// Error handling should return proper error response instead of null
			return null;
		}
	}

	/**
	 * Generates or retrieves a cached PDF slip for a short code.
	 * 
	 * Slip Generation Logic:
	 * This method manages the PDF slip for a short code, which contains:
	 * - Short code numeric value
	 * - Account number
	 * - Customer name
	 * - Instructions for use in Mpesa paybill transactions
	 * 
	 * Caching Strategy:
	 * - Check if slip PDF already exists in /tmp/ directory
	 * - If exists: Return cached file path (avoid regeneration)
	 * - If not exists: Query database for short code details, generate new PDF
	 * 
	 * Workflow Integration:
	 * - Invoked during approval phase to generate customer receipt
	 * - File is attached to approval notification email
	 * - Generated once and cached for future downloads
	 * 
	 * File Storage:
	 * - Location: /tmp/ directory (temporary storage)
	 * - Filename: {shortCode}.pdf (e.g., 350001.pdf)
	 * - Persistence: Remains until manually deleted or system cleanup
	 * 
	 * @param shortCode the numeric short code value
	 * @return the absolute file path to the generated or cached PDF slip,
	 *         null if generation fails
	 */
	public String generateSlip(int shortCode) {
		try {
			// Build filename from short code value
			String fileName = shortCode + ".pdf";
			// Construct file path in /tmp/ directory
			String filePath = "/tmp/" + fileName;
			File file = new File(filePath);
			
			// Check if slip has already been generated (caching)
			if(!file.exists()) {
				log.info("File does not exist, it has to be generated");
				
				// Retrieve short code details from database
				ShortCode shortCodeObj = shortCodeRepo.findByShortCode(shortCode);
				
				// Prepare data for PDF generation
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("accountNumber", shortCodeObj.getAccountNumber());
				data.put("accountName", shortCodeObj.getAccountName());
				data.put("shortCode", shortCodeObj.getShortCode());
				//data.put("sequenceNumber", shortCodeObj.getSequenceNumber());
				
				// Generate PDF slip with the prepared data
				SlipGenerator.generateShortCodeSlip(data);
			} else {
				log.info("File already exists");
			}
			
			// Return file path whether newly generated or cached
			return filePath;
		} catch(Exception e) {
			// Log and return null on error
			e.printStackTrace();
			return null;
		}
	}
}