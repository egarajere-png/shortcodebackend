package com.abcbank.shortcode.shortcode.utils;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ==========================================================
 * Hashing Utility Tests
 * ==========================================================
 *
 * Tests SHA-256 hashing functionality.
 *
 * Verifies:
 * - Correct hash generation
 * - Deterministic hashing
 * - Hash length
 * - Hexadecimal output
 * - Empty strings
 * - Unicode characters
 * - Special characters
 * - Null handling
 */
class HashingTest {

    private Hashing hashing;

    @BeforeEach
    void setUp() {
        hashing = new Hashing();
    }

    /*
     * ---------------------------------------------------------
     * Basic Hash Generation
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should generate SHA-256 hash")
    void shouldGenerateHash() {

        String hash = hashing.hash256("Hello World");

        assertNotNull(hash);
    }

    @Test
    @DisplayName("Hash should always be 64 hexadecimal characters")
    void shouldReturn64CharacterHash() {

        String hash = hashing.hash256("ABC Bank");

        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Hash should only contain hexadecimal characters")
    void shouldContainOnlyHexCharacters() {

        String hash = hashing.hash256("ShortCode");

        assertTrue(hash.matches("[0-9a-f]+"));
    }

    /*
     * ---------------------------------------------------------
     * Deterministic Behaviour
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Same input should always generate same hash")
    void shouldGenerateSameHashForSameInput() {

        String hash1 = hashing.hash256("abcdef");

        String hash2 = hashing.hash256("abcdef");

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Different inputs should generate different hashes")
    void shouldGenerateDifferentHashes() {

        String hash1 = hashing.hash256("abcdef");

        String hash2 = hashing.hash256("uvwxyz");

        assertNotEquals(hash1, hash2);
    }

    /*
     * ---------------------------------------------------------
     * Edge Cases
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should hash empty string")
    void shouldHashEmptyString() {

        String hash = hashing.hash256("");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should hash string containing spaces")
    void shouldHashSpaces() {

        String hash = hashing.hash256("   ");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should hash special characters")
    void shouldHashSpecialCharacters() {

        String hash = hashing.hash256("@#$%^&*()_+?><:{}");

        assertNotNull(hash);
    }

    @Test
    @DisplayName("Should hash unicode characters")
    void shouldHashUnicodeCharacters() {

        String hash = hashing.hash256("こんにちは世界");

        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    @DisplayName("Should hash numeric values")
    void shouldHashNumbers() {

        String hash = hashing.hash256("1234567890");

        assertNotNull(hash);
    }

    /*
     * ---------------------------------------------------------
     * Null Handling
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should throw NullPointerException when input is null")
    void shouldThrowExceptionForNullInput() {

        assertThrows(
                NullPointerException.class,
                () -> hashing.hash256(null)
        );
    }

}