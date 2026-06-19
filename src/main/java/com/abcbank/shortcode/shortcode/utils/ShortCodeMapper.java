package com.abcbank.shortcode.shortcode.utils;

import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.dto.ShortCodeDto;
import com.abcbank.shortcode.shortcode.entities.ShortCode;

/**
 * Mapper component for converting ShortCode entities to Data Transfer Objects (DTOs).
 * 
 * DTO Mapping Pattern:
 * This mapper implements the DTO pattern to decouple the internal entity model from
 * the API response model. It provides controlled serialization of database entities
 * to JSON responses, including timestamp formatting for API clients.
 * 
 * Mapping Responsibilities:
 * - Convert JPA entities (database objects) to DTOs (API response objects)
 * - Transform LocalDateTime timestamps to String format for JSON serialization
 * - Filter or transform fields as needed for API exposure
 * - Enable changes to entity structure without affecting API contracts
 * 
 * Usage:
 * Used throughout MainController in stream operations to transform lists of
 * ShortCode entities to ShortCodeDto for API responses.
 * 
 * Example:
 * shortCodeRepo.findByApproved(true)
 *     .stream()
 *     .map(shortCodeMapper::toDto)
 *     .toList();
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Component
public class ShortCodeMapper {

	/**
	 * Converts a ShortCode entity to a ShortCodeDto for API response.
	 * 
	 * Mapping Details:
	 * Maps all fields from the JPA entity to the DTO, with special handling for
	 * timestamp fields which are converted from LocalDateTime to String format
	 * for JSON serialization compatibility.
	 * 
	 * Field Transformations:
	 * - LocalDateTime fields: Converted to String using toString()
	 * - Null dates: Preserved as null (handled by null-safe assignment)
	 * - Boolean flags: Directly mapped
	 * - String fields: Directly mapped
	 * - Integer fields: Directly mapped
	 * 
	 * @param sc the ShortCode entity from database
	 * @return ShortCodeDto with all fields mapped for API response
	 */
    public ShortCodeDto toDto(ShortCode sc) {

        ShortCodeDto dto = new ShortCodeDto();

		// Map basic identification fields
        dto.setId(sc.getId());
        dto.setInitiator(sc.getInitiator());
        dto.setApprover(sc.getApprover());

		// Map account information fields
        dto.setAccountNumber(sc.getAccountNumber());
        dto.setAccountName(sc.getAccountName());
        dto.setPhoneNumber(sc.getPhoneNumber());
        dto.setEmailAddress(sc.getEmailAddress());
        dto.setIdNumber(sc.getIdNumber());
        dto.setCustId(sc.getCustId());

		// Map request and deletion remarks
        dto.setRemark(sc.getRemark());
        dto.setDeleteRemark(sc.getDeleteRemark());

		// Map short code details
        dto.setShortCode(sc.getShortCode());
        dto.setSequenceNumber(sc.getSequenceNumber());

		// Map timestamps with null-safe conversion to String format
        dto.setDateInitiated(
                sc.getDateInitiated() != null
                        ? sc.getDateInitiated().toString()
                        : null);

        dto.setDateApproved(
                sc.getDateApproved() != null
                        ? sc.getDateApproved().toString()
                        : null);

		// Map workflow state flags
        dto.setApproved(sc.isApproved());
        dto.setDeleteInitiated(sc.isDeleteInitiated());
        dto.setDeleted(sc.isDeleted());

        return dto;
    }
}