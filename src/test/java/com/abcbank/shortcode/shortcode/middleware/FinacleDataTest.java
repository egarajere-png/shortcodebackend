package com.abcbank.shortcode.shortcode.middleware;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for FinacleData.
 *
 * Tests include:
 * - Successful retrieval of shortcode from Finacle.
 * - Successful retrieval of account details.
 * - Correct Finacle endpoint URL construction.
 * - Proper interaction with RestTemplate.
 * - Correct JSON parsing of account information.
 */
@ExtendWith(MockitoExtension.class)
class FinacleDataTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FinacleData finacleData;

    private static final String HOST = "localhost:8080";

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                finacleData,
                "finqueryHost",
                HOST);
    }

    @Test
    void shouldFetchCBSShortCodeSuccessfully() {

        String accountNumber = "1234567890";

        String expectedUrl =
                "http://" + HOST +
                "/api/finacle/short-code/" +
                accountNumber;

        ResponseEntity<String> response =
                new ResponseEntity<>("654321", HttpStatus.OK);

        when(restTemplate.getForEntity(
                eq(expectedUrl),
                eq(String.class)))
                .thenReturn(response);

        String result =
                finacleData.fetchCBSShortCode(accountNumber);

        assertEquals("654321", result);

        verify(restTemplate)
                .getForEntity(expectedUrl, String.class);
    }

    @Test
    void shouldFetchAccountSuccessfully() throws Exception {

        String accountNumber = "1234567890";

        String expectedUrl =
                "http://" + HOST +
                "/api/finacle/account-data/" +
                accountNumber;

        String json =
                """
                {
                    "accountNumber":"1234567890",
                    "accountName":"John Doe",
                    "currency":"KES"
                }
                """;

        ResponseEntity<String> response =
                new ResponseEntity<>(json, HttpStatus.OK);

        when(restTemplate.getForEntity(
                eq(expectedUrl),
                eq(String.class)))
                .thenReturn(response);

        JSONObject result =
                finacleData.fetchAccount(accountNumber);

        assertNotNull(result);

        assertEquals(
                "1234567890",
                result.getString("accountNumber"));

        assertEquals(
                "John Doe",
                result.getString("accountName"));

        assertEquals(
                "KES",
                result.getString("currency"));

        verify(restTemplate)
                .getForEntity(expectedUrl, String.class);
    }

    @Test
    void shouldBuildCorrectShortCodeEndpoint() {

        String accountNumber = "999999999";

        String expectedUrl =
                "http://" + HOST +
                "/api/finacle/short-code/" +
                accountNumber;

        when(restTemplate.getForEntity(
                anyString(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>("111111", HttpStatus.OK));

        finacleData.fetchCBSShortCode(accountNumber);

        verify(restTemplate)
                .getForEntity(expectedUrl, String.class);
    }

    @Test
    void shouldBuildCorrectAccountEndpoint() {

        String accountNumber = "111222333";

        String expectedUrl =
                "http://" + HOST +
                "/api/finacle/account-data/" +
                accountNumber;

        when(restTemplate.getForEntity(
                anyString(),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        finacleData.fetchAccount(accountNumber);

        verify(restTemplate)
                .getForEntity(expectedUrl, String.class);
    }
}