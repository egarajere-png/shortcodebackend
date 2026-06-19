package com.abcbank.shortcode.shortcode.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.DTOAccount;

/**
 * Mock REST Controller for Finacle Core Banking System Integration.
 * 
 * This controller provides a mock/sandbox implementation of the Finacle core banking
 * system API endpoints. It is used for:
 * - Development and testing without connection to actual Finacle system
 * - Integration testing of account validation features
 * - Demonstration and training purposes
 * 
 * Endpoints:
 * - GET /api/finacle/account-data/{accountNumber} - Retrieve account information
 * - GET /mock/finacle/account-data/{accountNumber} - Alternative mock endpoint
 * 
 * Note: In production, this mock controller should be replaced with actual
 * Finacle adapter that connects to the real core banking system. The FinacleData
 * service handles the HTTP communication with the endpoint configured in properties.
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@RestController
@RequestMapping("/mock/finacle, /api/finacle")
public class MockFinacleController {

	/**
	 * Retrieves account information for a given account number.
	 * 
	 * Mock Implementation:
	 * This method returns hardcoded sample account data for demonstration purposes.
	 * In production, this would query the actual Finacle database via SOAP/HTTP interface.
	 * 
	 * Account Information:
	 * Returns the following account details:
	 * - Account number: as provided in request
	 * - Account name: Customer's full name
	 * - Customer ID: Internal core banking system identifier
	 * - ID Number: National identification number
	 * - Phone Number: Customer's registered phone
	 * - Email Address: Customer's registered email
	 * - Account Status: Current account status (ACTIVE, DORMANT, CLOSED, etc.)
	 * 
	 * Integration:
	 * This endpoint is called by the MainController.validate() method to retrieve
	 * and validate account information before short code creation.
	 * 
	 * @param accountNumber the bank account number to lookup
	 * @return DTOAccount with account information from the core banking system
	 */
	@GetMapping("/account-data/{accountNumber}")
	public DTOAccount getAccount(@PathVariable String accountNumber) {

		// Create account data transfer object
		DTOAccount account = new DTOAccount();

		// Populate with sample/mock data
		// In production: Query Finacle database with provided account number
		account.setAccountNumber(accountNumber);
		account.setAccountName("Egara Jere");
		account.setCustId("CUST001");
		account.setIdNumber("12345678");
		account.setPhoneNumber("0712345678");
		account.setEmailAddress("egara.jere@test.com");
		account.setAccountStatus("ACTIVE");

		return account;
	}
}