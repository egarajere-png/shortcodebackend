package com.abcbank.shortcode.shortcode.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.abcbank.shortcode.shortcode.entities.DTOAccount;
import com.abcbank.shortcode.shortcode.entities.DTOApproval;
import com.abcbank.shortcode.shortcode.entities.DTOResponse;
import com.abcbank.shortcode.shortcode.entities.DTOShortCode;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.AuditTrailService;
import com.abcbank.shortcode.shortcode.middleware.FinacleData;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.services.ExportService;
import com.abcbank.shortcode.shortcode.utils.ShortCodeMapper;

import org.springframework.web.client.RestTemplate;

/**
 * ==========================================================
 * Unit tests for MainController.
 *
 * Currently covers:
 * - validate()
 * ==========================================================
 */
@ExtendWith(MockitoExtension.class)
class MainControllerTest {

    @InjectMocks
    private MainController controller;

    @Mock
    private ShortCodeRepo shortCodeRepo;

    @Mock
    private FinacleData finacleData;

    @Mock
    private ShortCodeService shortCodeService;

    @Mock
    private UtilController utilController;

    @Mock
    private AuditTrailService auditTrailService;

    @Mock
    private AuditTrailRepo auditTrailRepo;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ExportService exportService;

    @Mock
    private ShortCodeMapper shortCodeMapper;

    private JSONObject accountJson;

    @BeforeEach
    void setUp() throws Exception{

        accountJson = new JSONObject();

        accountJson.put("accountName", "John Doe");
        accountJson.put("accountNumber", "123456789");
        accountJson.put("custId", "CUST001");
        accountJson.put("idNumber", "12345678");
        accountJson.put("emailAddress", "john@abc.com");
        accountJson.put("phoneNumber", "0712345678");
        accountJson.put("status", "ACTIVE");
    }

    /**
     * ----------------------------------------------------------
     * validate()
     * Should return account details when account exists.
     * ----------------------------------------------------------
     */
    @Test
    void shouldValidateAccountSuccessfully() {

        when(finacleData.fetchAccount("123456789"))
                .thenReturn(accountJson);

        DTOAccount result = controller.validate("123456789");

        assertNotNull(result);

        assertEquals("John Doe", result.getAccountName());
        assertEquals("123456789", result.getAccountNumber());
        assertEquals("CUST001", result.getCustId());
        assertEquals("12345678", result.getIdNumber());
        assertEquals("john@abc.com", result.getEmailAddress());
        assertEquals("0712345678", result.getPhoneNumber());
        assertEquals("ACTIVE", result.getAccountStatus());

        verify(finacleData).fetchAccount("123456789");
    }

    /**
     * ----------------------------------------------------------
     * validate()
     * Should use passport number when ID number is missing.
     * ----------------------------------------------------------
     */
    @Test
    void shouldUsePassportWhenIdNumberMissing()throws Exception{

        accountJson.remove("idNumber");
        accountJson.put("ppNumber", "P123456");

        when(finacleData.fetchAccount("123456789"))
                .thenReturn(accountJson);

        DTOAccount result = controller.validate("123456789");

        assertEquals("P123456", result.getIdNumber());
    }

    /**
     * ----------------------------------------------------------
     * validate()
     * Should return empty DTO when custId is missing.
     * ----------------------------------------------------------
     */
    @Test
    void shouldReturnEmptyAccountWhenCustIdMissing() {

        accountJson.remove("custId");

        when(finacleData.fetchAccount("123456789"))
                .thenReturn(accountJson);

        DTOAccount result = controller.validate("123456789");

        assertNotNull(result);

        assertNull(result.getCustId());
        assertNull(result.getAccountName());
        assertNull(result.getAccountNumber());
    }

    /**
     * ----------------------------------------------------------
     * validate()
     * Should return empty email when emailAddress is absent.
     * ----------------------------------------------------------
     */
    @Test
    void shouldHandleMissingEmailAddress() {

        accountJson.remove("emailAddress");

        when(finacleData.fetchAccount("123456789"))
                .thenReturn(accountJson);

        DTOAccount result = controller.validate("123456789");

        assertEquals("", result.getEmailAddress());
    }

    /**
 * ----------------------------------------------------------
 * Creates a valid ShortCode request for testing.
 * ----------------------------------------------------------
 */
private ShortCode createValidRequest() {

    ShortCode request = new ShortCode();

    request.setAccountNumber("123456789");
    request.setAccountName("John Doe");
    request.setCustId("CUST001");
    request.setIdNumber("12345678");
    request.setInitiator("maker");
    request.setEmailAddress("john@test.com");
    request.setPhoneNumber("0712345678");

    return request;
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should reject when account already has an approved shortcode.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectExistingApprovedShortcode() throws Exception {

    ShortCode request = createValidRequest();

    ShortCode existing = new ShortCode();
    existing.setApproved(true);

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            "123456789",
            true,
            false))
            .thenReturn(List.of(existing));

    DTOResponse response = controller.initiate(request);

    assertEquals("103", response.getStatusCode());
    assertEquals(
            "Shortcode is already granted for the account",
            response.getMessage());

    verify(shortCodeRepo, never()).save(any());
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should reject invalid request.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectInvalidRequest() throws Exception {

    ShortCode request = createValidRequest();

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            anyString(),
            eq(true),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeService.validateRequest(request))
            .thenReturn(false);

    DTOResponse response = controller.initiate(request);

    assertEquals("104", response.getStatusCode());
    assertEquals(
            "Some details are missing in the request",
            response.getMessage());
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should reject when another request is pending approval.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectPendingRequest() throws Exception {

    ShortCode request = createValidRequest();

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            anyString(),
            eq(true),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeService.validateRequest(any()))
            .thenReturn(true);

    when(shortCodeRepo.findByAccountNumberAndApproved(
            anyString(),
            eq(false)))
            .thenReturn(List.of(new ShortCode()));

    DTOResponse response = controller.initiate(request);

    assertEquals("101", response.getStatusCode());
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should reject preferred shortcode with fewer than 6 digits.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectInvalidPreferredShortcode() throws Exception {

    ShortCode request = createValidRequest();
    request.setShortCode(12345);

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            anyString(),
            eq(true),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeService.validateRequest(any()))
            .thenReturn(true);

    when(shortCodeRepo.findByAccountNumberAndApproved(
            anyString(),
            eq(false)))
            .thenReturn(List.of());

    DTOResponse response = controller.initiate(request);

    assertEquals("104", response.getStatusCode());
    assertEquals(
            "Preferred shortcode must be exactly 6 digits.",
            response.getMessage());
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should reject reserved shortcode.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectReservedShortcode() throws Exception {

    ShortCode request = createValidRequest();
    request.setShortCode(123456);

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            anyString(),
            eq(true),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeService.validateRequest(any()))
            .thenReturn(true);

    when(shortCodeRepo.findByAccountNumberAndApproved(
            anyString(),
            eq(false)))
            .thenReturn(List.of());

    DTOResponse response = controller.initiate(request);

    assertEquals("104", response.getStatusCode());
    assertEquals(
            "Selected shortcode is reserved.",
            response.getMessage());
}

/**
 * ----------------------------------------------------------
 * initiate()
 * Should initiate successfully using preferred shortcode.
 * ----------------------------------------------------------
 */
@Test
void shouldInitiateSuccessfullyWithPreferredShortcode() throws Exception {

    ShortCode request = createValidRequest();
    request.setShortCode(654321);

    when(shortCodeRepo.findByAccountNumberAndApprovedAndDeleted(
            anyString(),
            eq(true),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeService.validateRequest(any()))
            .thenReturn(true);

    when(shortCodeRepo.findByAccountNumberAndApproved(
            anyString(),
            eq(false)))
            .thenReturn(List.of());

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(null);

    when(shortCodeRepo.save(any(ShortCode.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    when(shortCodeService.generateHash(any()))
            .thenReturn("HASH123");

    DTOResponse response = controller.initiate(request);

    assertEquals("000", response.getStatusCode());
    assertEquals(654321, response.getShortCode());
    assertEquals(
            "Shortcode request initiated successfully",
            response.getMessage());

    verify(shortCodeRepo, times(2))
            .save(any(ShortCode.class));

    verify(shortCodeService)
            .generateHash(any());

    verify(auditTrailService)
            .logAction(
                    any(),
                    eq("INITIATE"),
                    eq("maker"),
                    eq("Shortcode request initiated"));
}

/**
 * ----------------------------------------------------------
 * approve()
 * Should return default response when no shortcode exists.
 * ----------------------------------------------------------
 */
@Test
void shouldReturnDefaultResponseWhenNoShortCodeExists() {

    DTOApproval request = new DTOApproval();
    request.setAccountNumber("123456789");
    request.setApprover("checker");

    when(shortCodeRepo.findByAccountNumberOrderByIdDesc("123456789"))
            .thenReturn(List.of());

    DTOResponse response = controller.approve(request);

    assertNotNull(response);
    assertNull(response.getStatusCode());

    verify(shortCodeRepo, never()).save(any());
}

/**
 * ----------------------------------------------------------
 * approve()
 * Should reject when integrity hash validation fails.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectWhenHashValidationFails() {

    DTOApproval request = new DTOApproval();
    request.setAccountNumber("123456789");
    request.setApprover("checker");

    ShortCode sc = createValidRequest();
    sc.setId(1);
    sc.setShortCode(654321);
    sc.setHash("OLD_HASH");

    when(shortCodeRepo.findByAccountNumberOrderByIdDesc(anyString()))
            .thenReturn(List.of(sc));

    when(shortCodeService.generateHash(sc))
            .thenReturn("NEW_HASH");

    DTOResponse response = controller.approve(request);

    assertEquals("104", response.getStatusCode());
    assertEquals(
            "Alarm: failed integrity check!",
            response.getMessage());

    verify(shortCodeRepo, never()).save(any());
}

/**
 * ----------------------------------------------------------
 * approve()
 * Should approve shortcode successfully.
 * ----------------------------------------------------------
 */
@Test
void shouldApproveSuccessfully() {

    DTOApproval request = new DTOApproval();
    request.setAccountNumber("123456789");
    request.setApprover("checker");

    ShortCode sc = createValidRequest();
    sc.setId(1);
    sc.setShortCode(654321);
    sc.setHash("HASH");

    when(shortCodeRepo.findByAccountNumberOrderByIdDesc(anyString()))
            .thenReturn(List.of(sc));

    when(shortCodeService.generateHash(any()))
            .thenReturn("HASH");

    when(shortCodeRepo.save(any()))
            .thenReturn(sc);

    when(utilController.generateSlip(654321))
            .thenReturn("/tmp/654321.pdf");

    DTOResponse response = controller.approve(request);

    assertEquals("000", response.getStatusCode());
    assertEquals(654321, response.getShortCode());

    verify(shortCodeRepo).save(any());

    verify(shortCodeService).sendReceiptEmail(sc);

    verify(utilController).generateSlip(654321);

    verify(auditTrailService).logAction(
            any(),
            eq("APPROVE"),
            eq("checker"),
            eq("Shortcode approved"));
}

/**
 * ----------------------------------------------------------
 * delete()
 * Should reject when shortcode does not exist.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectDeleteWhenShortCodeDoesNotExist() throws Exception {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);
    request.setAccountNumber("123456789");

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(null);

    DTOResponse response = controller.delete(request);

    assertEquals("104", response.getStatusCode());
    assertEquals(
            "Shortcode does not exist!",
            response.getMessage());
}

/**
 * ----------------------------------------------------------
 * delete()
 * Should reject when account number does not match.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectDeleteForWrongAccount() throws Exception {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);
    request.setAccountNumber("999999");

    ShortCode sc = createValidRequest();
    sc.setShortCode(654321);

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(sc);

    DTOResponse response = controller.delete(request);

    assertEquals("104", response.getStatusCode());
}

/**
 * ----------------------------------------------------------
 * delete()
 * Should initiate deletion successfully.
 * ----------------------------------------------------------
 */
@Test
void shouldInitiateDeleteSuccessfully() throws Exception {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);
    request.setAccountNumber("123456789");
    request.setDeleteRemark("Customer requested");

    ShortCode sc = createValidRequest();
    sc.setId(1);
    sc.setShortCode(654321);

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(sc);

    DTOResponse response = controller.delete(request);

    assertEquals("000", response.getStatusCode());

    verify(shortCodeRepo).save(sc);

    verify(auditTrailService).logAction(
            any(),
            eq("DELETE_REQUEST"),
            eq(sc.getInitiator()),
            eq("Customer requested"));
}

/**
 * ----------------------------------------------------------
 * approveDelete()
 * Should reject when shortcode does not exist.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectApproveDeleteWhenShortCodeMissing() throws Exception {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(null);

    DTOResponse response = controller.approveDelete(request);

    assertEquals("104", response.getStatusCode());
}

/**
 * ----------------------------------------------------------
 * approveDelete()
 * Should reject when account number mismatches.
 * ----------------------------------------------------------
 */
@Test
void shouldRejectApproveDeleteForWrongAccount() {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);
    request.setAccountNumber("111111");

    ShortCode sc = createValidRequest();
    sc.setShortCode(654321);

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(sc);

    DTOResponse response = controller.approveDelete(request);

    assertEquals("104", response.getStatusCode());
}

/**
 * ----------------------------------------------------------
 * approveDelete()
 * Should permanently delete shortcode.
 * ----------------------------------------------------------
 */
@Test
void shouldApproveDeleteSuccessfully() throws Exception {

    DTOShortCode request = new DTOShortCode();
    request.setShortCode(654321);
    request.setAccountNumber("123456789");

    ShortCode sc = createValidRequest();
    sc.setId(1);
    sc.setShortCode(654321);

    when(shortCodeRepo.findByShortCode(654321))
            .thenReturn(sc);

    DTOResponse response = controller.approveDelete(request);

    assertEquals("000", response.getStatusCode());

    assertTrue(sc.isDeleted());
    assertFalse(sc.isDeleteInitiated());

    verify(shortCodeRepo).save(sc);

    verify(auditTrailService).logAction(
            any(),
            eq("DELETE_APPROVE"),
            eq("123456789"),
            eq("Deletion approved"));
}


}