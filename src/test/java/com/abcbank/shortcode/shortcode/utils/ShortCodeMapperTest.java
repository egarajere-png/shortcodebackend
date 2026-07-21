package com.abcbank.shortcode.shortcode.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.abcbank.shortcode.shortcode.dto.ShortCodeDto;
import com.abcbank.shortcode.shortcode.entities.ShortCode;

/**
 * ==========================================================
 * ShortCodeMapper Tests
 * ==========================================================
 *
 * Tests entity-to-DTO conversion.
 *
 * Verifies:
 *
 * - Basic field mapping
 * - Timestamp conversion
 * - Boolean mapping
 * - Status generation
 * - Null date handling
 */
class ShortCodeMapperTest {

    private ShortCodeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ShortCodeMapper();
    }

    /**
     * Creates a fully populated ShortCode entity for testing.
     */
    private ShortCode createShortCode() {

        ShortCode sc = new ShortCode();

        sc.setId(1);

        sc.setInitiator("maker");
        sc.setApprover("checker");

        sc.setAccountNumber("123456789");
        sc.setAccountName("John Doe");

        sc.setPhoneNumber("0712345678");
        sc.setEmailAddress("john@abc.com");

        sc.setIdNumber("12345678");
        sc.setCustId("CUST001");

        sc.setRemark("Initiated");
        sc.setDeleteRemark("Delete Remark");

        sc.setShortCode(123456);
        sc.setPreferredShortCode(654321);
        sc.setSequenceNumber(7);

        sc.setDateInitiated(
                LocalDateTime.of(2026, 7, 20, 10, 30));

        sc.setDateApproved(
                LocalDateTime.of(2026, 7, 21, 11, 45));

        sc.setApproved(true);
        sc.setDeleteInitiated(false);
        sc.setDeleted(false);

        return sc;
    }

    /*
     * ---------------------------------------------------------
     * Field Mapping Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should map all entity fields")
    void shouldMapAllFields() {

        ShortCode entity = createShortCode();

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals(entity.getId(), dto.getId());

        assertEquals(entity.getInitiator(), dto.getInitiator());
        assertEquals(entity.getApprover(), dto.getApprover());

        assertEquals(entity.getAccountNumber(), dto.getAccountNumber());
        assertEquals(entity.getAccountName(), dto.getAccountName());

        assertEquals(entity.getPhoneNumber(), dto.getPhoneNumber());
        assertEquals(entity.getEmailAddress(), dto.getEmailAddress());

        assertEquals(entity.getIdNumber(), dto.getIdNumber());
        assertEquals(entity.getCustId(), dto.getCustId());

        assertEquals(entity.getRemark(), dto.getRemark());
        assertEquals(entity.getDeleteRemark(), dto.getDeleteRemark());

        assertEquals(entity.getShortCode(), dto.getShortCode());
        assertEquals(entity.getPreferredShortCode(),
                dto.getPreferredShortCode());

        assertEquals(entity.getSequenceNumber(),
                dto.getSequenceNumber());

        assertEquals(entity.isApproved(), dto.isApproved());

        assertEquals(entity.isDeleted(), dto.isDeleted());

        assertEquals(entity.isDeleteInitiated(),
                dto.isDeleteInitiated());
    }

    /*
     * ---------------------------------------------------------
     * Timestamp Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should convert timestamps to String")
    void shouldConvertDatesToStrings() {

        ShortCode entity = createShortCode();

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals(
                entity.getDateInitiated().toString(),
                dto.getDateInitiated());

        assertEquals(
                entity.getDateApproved().toString(),
                dto.getDateApproved());
    }

    @Test
    @DisplayName("Should allow null timestamps")
    void shouldHandleNullDates() {

        ShortCode entity = createShortCode();

        entity.setDateInitiated(null);
        entity.setDateApproved(null);

        ShortCodeDto dto = mapper.toDto(entity);

        assertNull(dto.getDateInitiated());
        assertNull(dto.getDateApproved());
    }

    /*
     * ---------------------------------------------------------
     * Status Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should return Active status")
    void shouldReturnActiveStatus() {

        ShortCode entity = createShortCode();

        entity.setApproved(true);
        entity.setDeleteInitiated(false);
        entity.setDeleted(false);

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals("Active", dto.getStatus());
    }

    @Test
    @DisplayName("Should return Pending Approval status")
    void shouldReturnPendingApprovalStatus() {

        ShortCode entity = createShortCode();

        entity.setApproved(false);
        entity.setDeleteInitiated(false);
        entity.setDeleted(false);

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals("Pending Approval", dto.getStatus());
    }

    @Test
    @DisplayName("Should return Pending Deletion status")
    void shouldReturnPendingDeletionStatus() {

        ShortCode entity = createShortCode();

        entity.setApproved(true);
        entity.setDeleteInitiated(true);
        entity.setDeleted(false);

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals("Pending Deletion", dto.getStatus());
    }

    @Test
    @DisplayName("Should return Deleted status")
    void shouldReturnDeletedStatus() {

        ShortCode entity = createShortCode();

        entity.setApproved(true);
        entity.setDeleteInitiated(true);
        entity.setDeleted(true);

        ShortCodeDto dto = mapper.toDto(entity);

        assertEquals("Deleted", dto.getStatus());
    }

    /*
     * ---------------------------------------------------------
     * Boolean Mapping Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should correctly map workflow flags")
    void shouldMapWorkflowFlags() {

        ShortCode entity = createShortCode();

        entity.setApproved(false);
        entity.setDeleteInitiated(true);
        entity.setDeleted(true);

        ShortCodeDto dto = mapper.toDto(entity);

        assertFalse(dto.isApproved());
        assertTrue(dto.isDeleteInitiated());
        assertTrue(dto.isDeleted());
    }
}