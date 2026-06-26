package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service for Finacle Core Banking System (CBS) integration.
 * 
 * This service provides methods to query the Finacle core banking system for account
 * information and short code data. It encapsulates HTTP communication with the
 * Finacle query endpoint using Spring's RestTemplate.
 * 
 * Integration Points:
 * - Account validation during short code initiation
 * - Short code lookup and verification in MainController
 * - Data consistency checks between application and CBS
 * 
 * Configuration:
 * Uses externalized configuration property service.params.finquery.host for
 * the Finacle query server endpoint, enabling easy switching between
 * development, testing, and production environments.
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Service
public class FinacleData {

	/**
	 * RestTemplate bean for making HTTP requests to Finacle endpoints.
	 * Provides simplified HTTP communication with automatic serialization/deserialization.
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Finacle query server host from application configuration.
	 * Example value: "10.0.0.1:8080" or "finacle.example.com"
	 * Configured via application.properties or application-{profile}.properties
	 */
	@Value("${service.params.finquery.host}")
	private String finqueryHost;

	/**
	 * Fetches the short code maintained in the Finacle core banking system for an account.
	 * 
	 * CBS Short Code Lookup:
	 * Queries the Finacle system to retrieve the short code currently maintained
	 * for a given account number. This is used for:
	 * - Validating data consistency between application and CBS
	 * - Ensuring the short code is properly registered in the core banking system
	 * - Supporting cross-validation in account lookup operations
	 * 
	 * Integration Point:
	 * Called from MainController.getAccountDetails() to verify that a short code
	 * exists in both the application database and the Finacle CBS system.
	 * 
	 * API Endpoint:
	 * GET /api/finacle/short-code/{accountNumber}
	 * Returns the numeric short code value as a string
	 * 
	 * Error Handling:
	 * Network errors or missing accounts return null or empty string.
	 * Caller should handle null responses gracefully.
	 * 
	 * @param accountNumber the bank account number to query
	 * @return the short code value as a string, or null/empty if not found
	 * 
	 * @throws RestClientException if HTTP communication with Finacle fails
	 */
	public String fetchCBSShortCode(String accountNumber) {
		// Build Finacle endpoint URL using configured host
		String endpoint = "http://" + finqueryHost + "/shortcodes/api/short-code/" + accountNumber;
		
		// Make HTTP GET request to Finacle and retrieve response as String
		ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
		
		// Return the short code value from the response body
		return response.getBody();
	}
}
