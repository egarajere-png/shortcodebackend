package com.abcbank.shortcode.shortcode.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.abcbank.shortcode.shortcode.entities.DTOAuthPayload;
import com.abcbank.shortcode.shortcode.entities.DTOAuthPayloadResponse;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles user authentication through Keycloak.
 *
 * Receives user credentials, requests a JWT access token
 * from Keycloak, and returns it to the client.
 */

@Slf4j
@RestController


public class AuthController {
	
	/**
	 * Keycloak token endpoint URL from application configuration.
	 * Typically: https://keycloak.example.com/auth/realms/shortcode/protocol/openid-connect/token
	 */
	@Value("${keycloak.config.token-url}")
    String KEYCLOAK_URL;

	/**
	 * Keycloak client identifier for this application.
	 * Used in token requests to identify which application is requesting the token.
	 */
	@Value("${keycloak.config.client-id}")
	private String kcClientId;
	
	/**
	 * Repository for short code database operations.
	 * Injected but not used in AuthController; could be removed in future refactoring.
	 */
	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	/**
	 * Service for short code business logic operations.
	 * Injected but not used in AuthController; could be removed in future refactoring.
	 */
	@Autowired
	ShortCodeService shortCodeService;

	/**
 * Authenticates a user and returns a JWT access token.
 *
 * @param authPayload login credentials
 * @return authentication response
 */
	@PostMapping("/shortcodes/api/get-token")
    public DTOAuthPayloadResponse authenticateUser(@RequestBody DTOAuthPayload authPayload) {
        
		// Prepare HTTP headers for form-encoded authentication request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Build OAuth 2.0 Resource Owner Password Grant request parameters
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", kcClientId);
        map.add("username", authPayload.getUsername());
        map.add("password", authPayload.getPassword());
        map.add("grant_type", "password");
        
		log.info("Authentication Request: {}", map);


		// Create HTTP entity with headers and body
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        
        try {
			// Send POST request to Keycloak token endpoint and parse response
            return new RestTemplate().exchange(KEYCLOAK_URL,
                    HttpMethod.POST,
                    entity,
                    DTOAuthPayloadResponse.class
            ).getBody();
        } catch (Exception exception) {

			// Log authentication failures and return empty response
            log.info(exception.getLocalizedMessage());
            String responseError = exception.getLocalizedMessage().replace("400 Bad Request: ", "");
            log.error(responseError);
            return new DTOAuthPayloadResponse();
        }
			
    }
}