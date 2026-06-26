package com.abcbank.shortcode.shortcode.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.abcbank.shortcode.shortcode.entities.ShortCode;

/**
 * Spring Data JPA Repository for ShortCode entity database operations.
 * 
 * This repository provides CRUD (Create, Read, Update, Delete) operations and
 * custom query methods for the ShortCode entity. It encapsulates all database
 * interactions related to short code records.
 * 
 * Query Methods:
 * Implement the query-by-method-name pattern where method names define the SQL query:
 * - findByApproved: WHERE approved = ?
 * - findByDeleted: WHERE deleted = ?
 * - findByAccountNumberAndApproved: WHERE accountNumber = ? AND approved = ?
 * - findByAccountNumberOrderByIdDesc: WHERE accountNumber = ? ORDER BY id DESC
 * 
 * Database Integration:
 * - Auto-generates SQL queries from method signatures
 * - Maps ResultSet to ShortCode entity objects
 * - Handles transaction management
 * - Provides pagination and sorting support
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
public interface ShortCodeRepo extends CrudRepository<ShortCode, Integer> {
	/**
	 * Retrieves all short code records from the database.
	 * 
	 * @return List of all ShortCode entities, empty list if none exist
	 */
	List<ShortCode> findAll();

	/**
	 * Finds a short code record by its database ID.
	 * 
	 * @param id the database identifier
	 * @return the ShortCode entity if found, null otherwise
	 */
	ShortCode findById(int id);

	/**
	 * Finds all short code records with a specific deletion status.
	 * 
	 * @param isDeleted true to find deleted records, false to find active records
	 * @return List of ShortCode entities with matching deletion status
	 */
	List<ShortCode> findByDeleted(boolean isDeleted);

	/**
	 * Finds all short code records with a specific approval status.
	 * 
	 * @param isApproved true to find approved records, false to find pending requests
	 * @return List of ShortCode entities with matching approval status
	 */
	List<ShortCode> findByApproved(boolean isApproved);

	/**
	 * Finds all short code records for a given account, ordered by newest first.
	 * 
	 * Used to retrieve short code history for a customer account.
	 * Most recent request appears first (descending ID order).
	 * 
	 * @param accountNumber the account to search for
	 * @return List of ShortCode entities ordered by ID descending
	 */
	List<ShortCode> findByAccountNumberOrderByIdDesc(String accountNumber);

	/**
	 * Finds all short code requests for an account with a specific approval status.
	 * 
	 * Used to check for pending approval requests or approved short codes.
	 * 
	 * @param accountNumber the account to search for
	 * @param approved true for approved requests, false for pending
	 * @return List of matching ShortCode entities
	 */
	List<ShortCode> findByAccountNumberAndApproved(String accountNumber, boolean approved);

	/**
	 * Finds approved and active short code records for a specific account.
	 * 
	 * Used in the initiate workflow to check if an account already has an
	 * active (approved and not deleted) short code.
	 * 
	 * Business Rule: Only one active short code per account is allowed.
	 * 
	 * @param accountNumber the account to search for
	 * @param approved must be true (only approved records)
	 * @param deleted must be false (only active records)
	 * @return List of matching ShortCode entities, typically empty or single-element
	 */
	List<ShortCode> findByAccountNumberAndApprovedAndDeleted(String accountNumber, boolean approved, boolean deleted);

	/**
	 * Finds short code records with pending deletion approval.
	 * 
	 * Used to retrieve deletion requests awaiting Checker approval.
	 * Returns records where deletion was initiated but not yet finalized.
	 * 
	 * @param initiated true (deletion requested)
	 * @param deleted false (deletion not yet finalized)
	 * @return List of ShortCode entities with pending deletion
	 */
	List<ShortCode> findByDeleteInitiatedAndDeleted(boolean initiated, boolean deleted);

	/**
	 * Finds a short code record by its numeric short code value.
	 * 
	 * Used to lookup short code details by the actual short code number.
	 * The numeric value is the customer-visible short code (e.g., 350001).
	 * 
	 * @param shortCode the numeric short code value
	 * @return the ShortCode entity if found, null otherwise
	 */
	ShortCode findByShortCode(int shortCode);
	ShortCode findByAccountNumber(String accountNumber);
}