package com.abcbank.shortcode.shortcode.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.utils.Emailer;
import com.abcbank.shortcode.shortcode.utils.Hashing;

/**
 * Unit tests for ShortCodeService.
 *
 * Tests include:
 * - Sending receipt emails.
 * - Validation of mandatory request fields.
 * - SHA-256 hash generation.
 * - Proper handling of null values during hashing.
 * - Trimming of whitespace before hashing.
 */
@ExtendWith(MockitoExtension.class)
class ShortCodeServiceTest {

    @Mock
    private Emailer emailer;

    @Mock
    private Hashing hashing;

    @InjectMocks
    private ShortCodeService shortCodeService;

    private ShortCode shortCode;

    @BeforeEach
    void setUp() {

        shortCode = new ShortCode();

        shortCode.setId(1);
        shortCode.setAccountNumber("123456789");
        shortCode.setCustId("CUST001");
        shortCode.setAccountName("John Doe");
        shortCode.setIdNumber("12345678");
        shortCode.setEmailAddress("John.Doe@EMAIL.COM");
        shortCode.setPhoneNumber("0712345678");
        shortCode.setShortCode(654321);
        shortCode.setApproved(true);
        shortCode.setDeleted(false);
        shortCode.setInitiator("maker1");
    }

    @Test
    void shouldSendReceiptEmailSuccessfully() {

        shortCodeService.sendReceiptEmail(shortCode);

        verify(emailer).send(
                eq("ABC Bank Support<talk2us@abcthebank.com>"),
                eq("john.doe@email.com"),
                eq(""),
                eq("ABC Bank - New Short-code 654321"),
                contains("654321"),
                eq("/tmp/654321.pdf")
        );
    }

    @Test
    void shouldValidateRequestWhenAllMandatoryFieldsExist() {

        assertTrue(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldReturnFalseWhenAccountNameIsMissing() {

        shortCode.setAccountName(null);

        assertFalse(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldReturnFalseWhenAccountNumberIsMissing() {

        shortCode.setAccountNumber(null);

        assertFalse(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldReturnFalseWhenIdNumberIsMissing() {

        shortCode.setIdNumber(null);

        assertFalse(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldReturnFalseWhenInitiatorIsMissing() {

        shortCode.setInitiator(null);

        assertFalse(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldReturnFalseWhenCustomerIdIsMissing() {

        shortCode.setCustId(null);

        assertFalse(shortCodeService.validateRequest(shortCode));
    }

    @Test
    void shouldGenerateHashUsingHashingUtility() {

        when(hashing.hash256(anyString()))
                .thenReturn("mockHash");

        String result = shortCodeService.generateHash(shortCode);

        assertEquals("mockHash", result);

        verify(hashing).hash256(anyString());
    }

    @Test
    void shouldGenerateCorrectHashSourceData() {

        when(hashing.hash256(anyString()))
                .thenReturn("hash");

        shortCodeService.generateHash(shortCode);

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(hashing).hash256(captor.capture());

        assertEquals(
                "1|123456789|CUST001|John Doe|12345678|John.Doe@EMAIL.COM|0712345678|654321|true|false",
                captor.getValue()
        );
    }

    @Test
    void shouldReplaceNullValuesWithEmptyStringsWhenGeneratingHash() {

        shortCode.setAccountName(null);
        shortCode.setEmailAddress(null);
        shortCode.setPhoneNumber(null);

        when(hashing.hash256(anyString()))
                .thenReturn("hash");

        shortCodeService.generateHash(shortCode);

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(hashing).hash256(captor.capture());

        assertTrue(captor.getValue().contains("||"));
    }

    @Test
    void shouldTrimWhitespaceBeforeGeneratingHash() {

        shortCode.setAccountName("  John Doe  ");
        shortCode.setEmailAddress("  john@email.com  ");

        when(hashing.hash256(anyString()))
                .thenReturn("hash");

        shortCodeService.generateHash(shortCode);

        ArgumentCaptor<String> captor =
                ArgumentCaptor.forClass(String.class);

        verify(hashing).hash256(captor.capture());

        String hashSource = captor.getValue();

        assertTrue(hashSource.contains("John Doe"));
        assertTrue(hashSource.contains("john@email.com"));

        assertFalse(hashSource.contains("  "));
    }
}