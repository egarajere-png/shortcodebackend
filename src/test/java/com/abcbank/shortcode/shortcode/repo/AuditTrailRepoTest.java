package com.abcbank.shortcode.shortcode.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import com.abcbank.shortcode.shortcode.entities.AuditTrail;

import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AuditTrailRepoTest {

    @Autowired
    private AuditTrailRepo repository;

    private AuditTrail createAudit(
            Integer shortCodeId,
            String accountNumber,
            Integer shortCode,
            String action,
            LocalDateTime actionDate) {

        AuditTrail audit = new AuditTrail();

        audit.setShortCodeId(shortCodeId);
        audit.setAccountNumber(accountNumber);
        audit.setShortCode(shortCode);

        audit.setAction(action);
        audit.setPerformedBy("tester");
        audit.setRemarks("Test");

        audit.setActionDate(actionDate);

        return repository.save(audit);
    }

    @Test
@DisplayName("Should find audit trail by shortcode id ordered descending")
void shouldFindByShortCodeIdOrderByActionDateDesc() {

    createAudit(
            1,
            "123456789",
            350001,
            "INITIATE",
            LocalDateTime.now().minusHours(2));

    createAudit(
            1,
            "123456789",
            350001,
            "APPROVE",
            LocalDateTime.now());

    List<AuditTrail> results =
            repository.findByShortCodeIdOrderByActionDateDesc(1);

    assertEquals(2, results.size());

    assertEquals(
            "APPROVE",
            results.get(0).getAction());

    assertEquals(
            "INITIATE",
            results.get(1).getAction());
}

@Test
@DisplayName("Should find audit trail by account number ordered descending")
void shouldFindByAccountNumberOrderByActionDateDesc() {

    createAudit(
            1,
            "ACC001",
            350001,
            "INITIATE",
            LocalDateTime.now().minusHours(2));

    createAudit(
            2,
            "ACC001",
            350002,
            "APPROVE",
            LocalDateTime.now());

    List<AuditTrail> results =
            repository.findByAccountNumberOrderByActionDateDesc("ACC001");

    assertEquals(2, results.size());
    assertEquals("APPROVE", results.get(0).getAction());
    assertEquals("INITIATE", results.get(1).getAction());
}

@Test
@DisplayName("Should find audit trail by shortcode ordered descending")
void shouldFindByShortCodeOrderByActionDateDesc() {

    createAudit(
            1,
            "ACC001",
            350001,
            "INITIATE",
            LocalDateTime.now().minusHours(1));

    createAudit(
            1,
            "ACC002",
            350001,
            "APPROVE",
            LocalDateTime.now());

    List<AuditTrail> results =
            repository.findByShortCodeOrderByActionDateDesc(350001);

    assertEquals(2, results.size());
    assertEquals("APPROVE", results.get(0).getAction());
    assertEquals("INITIATE", results.get(1).getAction());
}

@Test
@DisplayName("Should return latest audit entries using pageable")
void shouldFindLatestAuditEntries() {

    createAudit(
            1,
            "ACC001",
            350001,
            "INITIATE",
            LocalDateTime.now().minusHours(3));

    createAudit(
            2,
            "ACC002",
            350002,
            "APPROVE",
            LocalDateTime.now().minusHours(2));

    createAudit(
            3,
            "ACC003",
            350003,
            "DELETE",
            LocalDateTime.now());

    List<AuditTrail> results =
            repository.findAllByOrderByActionDateDesc(
                    PageRequest.of(0, 2));

    assertEquals(2, results.size());

    assertEquals("DELETE", results.get(0).getAction());
    assertEquals("APPROVE", results.get(1).getAction());
}
}