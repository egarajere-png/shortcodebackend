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
 * REST Controller for authentication and authorization operations.
 * 
 * This controller integrates with Keycloak identity and access management (IAM) server
 * to authenticate users and issue OAuth 2.0 access tokens. It acts as a bridge between
 * client applications and the Keycloak authentication server.
 * 
 * Authorization & Authentication Flow:
 * 1. Client submits username and password to /shortcodes/api/get-token
 * 2. Controller forwards credentials to Keycloak using Resource Owner Password Grant
 * 3. Keycloak validates credentials and returns JWT access token
 * 4. Token includes user roles (maker, checker, apicaller) for role-based access control
 * 5. Client uses token for subsequent API requests (validated via KeycloakConfig)
 * 
 * Security Considerations:
 * - Password Grant should only be used for trusted client applications
 * - Tokens should be transmitted over HTTPS only
 * - Token expiration enforces re-authentication periodically
 * - User roles are validated server-side for all protected endpoints
 * 
 * @author ABC Bank Development Team
 * @version 1.0
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
	 * Authenticates a user with Keycloak and returns an OAuth 2.0 access token.
	 * 
	 * Authentication Flow:
	 * 1. Receives username and password from client
	 * 2. Constructs OAuth 2.0 Resource Owner Password Grant request
	 * 3. Sends credentials to Keycloak token endpoint
	 * 4. Returns JWT token with user roles and permissions
	 * 
	 * Token Usage:
	 * The returned access token contains:
	 * - User identity and roles (maker, checker, apicaller)
	 * - Token expiration time (typically 5 minutes)
	 * - Refresh token for obtaining new access tokens without re-authenticating
	 * - Client scope and allowed operations
	 * 
	 * Error Handling:
	 * - Invalid credentials return Keycloak error message
	 * - Connection errors are logged and an empty response is returned
	 * - Client should check for missing access_token to detect failures
	 * 
	 * Role-Based Access Control:
	 * After receiving the token, users can perform actions according to their roles:
	 * - Maker: Initiate and delete short code requests
	 * - Checker: Approve and delete approved short codes
	 * - API Caller: Call APIs for integration with other systems
	 * 
	 * @param authPayload contains the username and password for authentication
	 * @return DTOAuthPayloadResponse with access_token and expires_in, or empty response on error
	 * 
	 * @throws RestClientException if communication with Keycloak server fails
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