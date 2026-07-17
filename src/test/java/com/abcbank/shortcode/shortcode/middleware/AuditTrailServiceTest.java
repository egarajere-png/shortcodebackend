package com.abcbank.shortcode.shortcode.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;

/**
 * Unit tests for AuditTrailService.
 *
 * Tests include:
 * - Successful audit trail creation.
 * - Correct mapping of ShortCode fields.
 * - Correct action information.
 * - Audit timestamp generation.
 * - Persistence using AuditTrailRepo.
 */
@ExtendWith(MockitoExtension.class)
class AuditTrailServiceTest {

    @Mock
    private AuditTrailRepo auditTrailRepo;

    @InjectMocks
    private AuditTrailService auditTrailService;

    private ShortCode shortCode;

    @BeforeEach
    void setUp() {

        shortCode = new ShortCode();

        shortCode.setId(100);
        shortCode.setAccountNumber("1234567890");
        shortCode.setShortCode(654321);
    }

    @Test
    void shouldSaveAuditTrailSuccessfully() {

        auditTrailService.logAction(
                shortCode,
                "APPROVED",
                "checker1",
                "Request approved successfully");

        ArgumentCaptor<AuditTrail> captor =
                ArgumentCaptor.forClass(AuditTrail.class);

        verify(auditTrailRepo).save(captor.capture());

        AuditTrail savedAudit = captor.getValue();

        assertEquals(100, savedAudit.getShortCodeId());
        assertEquals("1234567890", savedAudit.getAccountNumber());
        assertEquals(654321, savedAudit.getShortCode());

        assertEquals("APPROVED", savedAudit.getAction());
        assertEquals("checker1", savedAudit.getPerformedBy());
        assertEquals(
                "Request approved successfully",
                savedAudit.getRemarks());

        assertNotNull(savedAudit.getActionDate());
    }

    @Test
    void shouldGenerateCurrentTimestamp() {

        LocalDateTime before = LocalDateTime.now();

        auditTrailService.logAction(
                shortCode,
                "CREATED",
                "maker1",
                "Initial request");

        LocalDateTime after = LocalDateTime.now();

        ArgumentCaptor<AuditTrail> captor =
                ArgumentCaptor.forClass(AuditTrail.class);

        verify(auditTrailRepo).save(captor.capture());

        LocalDateTime actionDate =
                captor.getValue().getActionDate();

        assertFalse(actionDate.isBefore(before));
        assertFalse(actionDate.isAfter(after));
    }

    @Test
    void shouldPersistAuditTrailExactlyOnce() {

        auditTrailService.logAction(
                shortCode,
                "DELETE",
                "maker2",
                "Deletion requested");

        verify(auditTrailRepo).save(any(AuditTrail.class));
    }
}