package com.abcbank.shortcode.shortcode.repo;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.abcbank.shortcode.shortcode.entities.ShortCode;

import org.springframework.test.context.ActiveProfiles;

/**
 * ==========================================================
 * ShortCode Repository Tests
 * ==========================================================
 *
 * Tests all custom repository query methods using
 * an in-memory H2 database.
 *
 * Verifies:
 *
 * - CRUD queries
 * - Filter queries
 * - Exists queries
 * - Dashboard count queries
 * - JPQL queries
 * - Ordering
 *
 * No mocking is used.
 */
@DataJpaTest
@ActiveProfiles("test")
class ShortCodeRepoTest {

    @Autowired
    private ShortCodeRepo repository;

    /**
     * ---------------------------------------------------------
     * Helper Methods
     * ---------------------------------------------------------
     */

    /**
     * Creates and persists a ShortCode entity for testing.
     */
    private ShortCode createShortCode(
            String accountNumber,
            int shortCode,
            Integer preferredShortCode,
            boolean approved,
            boolean deleted,
            boolean deleteInitiated,
            LocalDateTime initiatedDate) {

        ShortCode sc = new ShortCode();

        sc.setInitiator("maker");
        sc.setApprover("checker");

        sc.setAccountNumber(accountNumber);
        sc.setAccountName("John Doe");

        sc.setPhoneNumber("0712345678");
        sc.setEmailAddress("john@test.com");

        sc.setIdNumber("12345678");
        sc.setCustId("C001");

        sc.setRemark("Test");
        sc.setDeleteRemark("");

        sc.setShortCode(shortCode);
        sc.setPreferredShortCode(preferredShortCode);

        sc.setSequenceNumber(1);

        sc.setDateInitiated(initiatedDate);
        sc.setDateApproved(LocalDateTime.now());

        sc.setApproved(approved);
        sc.setDeleted(deleted);
        sc.setDeleteInitiated(deleteInitiated);

        sc.setHash("HASH");

        return repository.save(sc);
    }

    private ShortCode createApprovedShortCode() {
    return createShortCode(
            "123456789",
            111111,
            900001,
            true,
            false,
            false,
            LocalDateTime.now());
}

private ShortCode createPendingShortCode() {
    return createShortCode(
            "123456789",
            222222,
            900002,
            false,
            false,
            false,
            LocalDateTime.now());
}

        /*
     * ---------------------------------------------------------
     * Save Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should save shortcode successfully")
    void shouldSaveShortCode() {

        ShortCode saved =
                createShortCode(
                        "001",
                        350001,
                        450001,
                        true,
                        false,
                        false,
                        LocalDateTime.now());

        assertNotNull(saved);
        assertTrue(saved.getId() > 0);
    }

    /*
     * ---------------------------------------------------------
     * Find All Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should find all records")
    void shouldFindAll() {

        createShortCode(
                "001",
                111111,
                211111,
                true,
                false,
                false,
                LocalDateTime.now());

        createShortCode(
                "002",
                222222,
                322222,
                false,
                false,
                false,
                LocalDateTime.now());

        List<ShortCode> results =
                repository.findAll();

        assertEquals(2, results.size());
    }

    /*
     * ---------------------------------------------------------
     * Find By Id Tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should find shortcode by id")
    void shouldFindById() {

        ShortCode saved =
                createShortCode(
                        "001",
                        123456,
                        223456,
                        true,
                        false,
                        false,
                        LocalDateTime.now());

        ShortCode found =
                repository.findById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
    }

    @Test
    @DisplayName("Should return null for unknown id")
    void shouldReturnNullForUnknownId() {

        ShortCode result =
                repository.findById(9999);

        assertNull(result);
    }
    
    /*
 * ---------------------------------------------------------
 * Find By Account Tests
 * ---------------------------------------------------------
 */

@Test
@DisplayName("Should find by account number ordered by id desc")
void shouldFindByAccountNumberOrderByIdDesc() {

    ShortCode first = createApprovedShortCode();
    ShortCode second = createPendingShortCode();

    List<ShortCode> results =
            repository.findByAccountNumberOrderByIdDesc("123456789");

    assertEquals(2, results.size());
    assertEquals(second.getId(), results.get(0).getId());
    assertEquals(first.getId(), results.get(1).getId());
}

@Test
@DisplayName("Should find approved records by account")
void shouldFindByAccountNumberAndApproved() {

    createApprovedShortCode();
    createPendingShortCode();

    List<ShortCode> results =
            repository.findByAccountNumberAndApproved(
                    "123456789",
                    true);

    assertEquals(1, results.size());
    assertTrue(results.get(0).isApproved());
}

@Test
@DisplayName("Should find approved and not deleted")
void shouldFindApprovedAndDeleted() {

    createApprovedShortCode();
    createPendingShortCode();

    List<ShortCode> results =
            repository.findByApprovedAndDeleted(
                    true,
                    false);

    assertEquals(1, results.size());
}

@Test
@DisplayName("Should find active records")
void shouldFindDeletedFalse() {

    createApprovedShortCode();
    createPendingShortCode();

    List<ShortCode> results =
            repository.findByDeleted(false);

    assertEquals(2, results.size());
}

@Test
@DisplayName("Should find active records ordered")
void shouldFindDeletedOrderByIdDesc() {

    createApprovedShortCode();
    createPendingShortCode();

    List<ShortCode> results =
            repository.findByDeletedOrderByIdDesc(false);

    assertEquals(2, results.size());

    assertTrue(
            results.get(0).getId() >
            results.get(1).getId());
}

@Test
@DisplayName("Should find by shortcode")
void shouldFindByShortCode() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    ShortCode result =
            repository.findByShortCode(111111);

    assertNotNull(result);
    assertEquals(111111, result.getShortCode());
}

@Test
@DisplayName("Should find preferred shortcode")
void shouldFindPreferredShortCode() {

    createShortCode(
            "001",
            111111,
            900001,
            true,
            false,
            false,
            LocalDateTime.now());

    ShortCode result =
            repository.findByPreferredShortCode(900001);

    assertNotNull(result);
    assertEquals(900001, result.getPreferredShortCode());
}

@Test
@DisplayName("Should find by account number")
void shouldFindByAccountNumber() {

    createShortCode(
            "ACC001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    ShortCode result =
            repository.findByAccountNumber("ACC001");

    assertNotNull(result);
    assertEquals("ACC001", result.getAccountNumber());
}

@Test
@DisplayName("Should detect existing shortcode")
void shouldExistByShortCode() {

    createShortCode(
            "001",
            123456,
            223456,
            true,
            false,
            false,
            LocalDateTime.now());

    assertTrue(repository.existsByShortCode(123456));
    assertFalse(repository.existsByShortCode(999999));
}

@Test
@DisplayName("Should detect preferred shortcode")
void shouldExistPreferredShortCode() {

    createShortCode(
            "001",
            111111,
            900001,
            true,
            false,
            false,
            LocalDateTime.now());

    assertTrue(repository.existsByPreferredShortCode(900001));
    assertFalse(repository.existsByPreferredShortCode(123123));
}


@Test
@DisplayName("Should find deleted records")
void shouldFindDeleted() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    ShortCode deleted =
            createShortCode(
                    "002",
                    222222,
                    322222,
                    true,
                    true,
                    false,
                    LocalDateTime.now());

    List<ShortCode> results =
            repository.findByDeleted(true);

    assertEquals(1, results.size());
    assertEquals(deleted.getId(), results.get(0).getId());
}

@Test
@DisplayName("Should find by account number, approved and deleted")
void shouldFindByAccountNumberAndApprovedAndDeleted() {

    createShortCode(
            "ACC001",
            111111,
            211111,
            true,
            true,
            false,
            LocalDateTime.now());

    ShortCode expected =
            createShortCode(
                    "ACC001",
                    222222,
                    322222,
                    true,
                    false,
                    false,
                    LocalDateTime.now());

    createShortCode(
            "ACC001",
            333333,
            433333,
            false,
            false,
            false,
            LocalDateTime.now());

    List<ShortCode> results =
            repository.findByAccountNumberAndApprovedAndDeleted(
                    "ACC001",
                    true,
                    false);

    assertEquals(1, results.size());
    assertEquals(expected.getId(), results.get(0).getId());
}

@Test
@DisplayName("Should find delete initiated records")
void shouldFindByDeleteInitiatedAndDeleted() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    ShortCode expected =
            createShortCode(
                    "002",
                    222222,
                    322222,
                    true,
                    false,
                    true,
                    LocalDateTime.now());

    List<ShortCode> results =
            repository.findByDeleteInitiatedAndDeleted(
                    true,
                    false);

    assertEquals(1, results.size());
    assertEquals(expected.getId(), results.get(0).getId());
}

@Test
@DisplayName("Should detect shortcode with deleted flag")
void shouldExistByShortCodeAndDeleted() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    assertTrue(
            repository.existsByShortCodeAndDeleted(
                    111111,
                    false));

    assertFalse(
            repository.existsByShortCodeAndDeleted(
                    111111,
                    true));
}

@Test
@DisplayName("Should count approved records")
void shouldCountByApproved() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    createShortCode(
            "002",
            222222,
            322222,
            false,
            false,
            false,
            LocalDateTime.now());

    assertEquals(1, repository.countByApproved(true));
    assertEquals(1, repository.countByApproved(false));
}


@Test
@DisplayName("Should count approved and active records")
void shouldCountByApprovedAndDeleted() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now());

    createShortCode(
            "002",
            222222,
            322222,
            false,
            false,
            false,
            LocalDateTime.now());

    createShortCode(
            "003",
            333333,
            433333,
            true,
            true,
            false,
            LocalDateTime.now());

    assertEquals(
            1,
            repository.countByApprovedAndDeleted(
                    true,
                    false));
}
@Test
@DisplayName("Should count delete initiated records")
void shouldCountByDeleteInitiated() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            true,
            LocalDateTime.now());

    createShortCode(
            "002",
            222222,
            322222,
            true,
            false,
            false,
            LocalDateTime.now());

    assertEquals(1, repository.countByDeleteInitiated(true));
    assertEquals(1, repository.countByDeleteInitiated(false));
}

@Test
@DisplayName("Should count delete initiated and active records")
void shouldCountByDeleteInitiatedAndDeleted() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            true,
            LocalDateTime.now());

    createShortCode(
            "002",
            222222,
            322222,
            true,
            true,
            true,
            LocalDateTime.now());

    assertEquals(
            1,
            repository.countByDeleteInitiatedAndDeleted(
                    true,
                    false));
}

@Test
@DisplayName("Should count records initiated between dates")
void shouldCountInitiatedBetween() {

    createShortCode(
            "001",
            111111,
            211111,
            true,
            false,
            false,
            LocalDateTime.now().minusDays(1));

    createShortCode(
            "002",
            222222,
            322222,
            true,
            false,
            false,
            LocalDateTime.now().minusDays(10));

    long count =
            repository.countInitiatedBetween(
                    LocalDateTime.now().minusDays(2),
                    LocalDateTime.now());

    assertEquals(1, count);
}

}
    