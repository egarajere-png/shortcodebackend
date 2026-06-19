package com.abcbank.shortcode.shortcode.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object for OAuth 2.0 authentication response from Keycloak.
 * 
 * This DTO represents the response received from the Keycloak token endpoint
 * after successful user authentication. It contains the JWT access token and
 * related token information required for API authorization.
 * 
 * Response Fields:
 * - access_token: JWT token for authorizing API requests
 * - expires_in: Lifetime of the access token in seconds
 * - refresh_expires_in: Lifetime of refresh token in seconds
 * - refresh_token: Token used to obtain new access tokens without re-authenticating
 * - token_type: Type of token (usually "Bearer")
 * - session_state: Keycloak session identifier
 * - scope: Granted scope/permissions
 * - notBeforePolicy: Token validity start time
 * 
 * Usage:
 * - Returned by Keycloak /token endpoint
 * - Client extracts access_token for API authorization
 * - Access token included in Authorization: Bearer header for API requests
 * - Refresh token used to obtain new access token after expiration
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOAuthPayloadResponse {
    /**
     * JWT access token for authorizing API requests.
     * Must be included in Authorization header: Bearer {access_token}
     */
    public String access_token;

    /**
     * Lifetime of the access token in seconds.
     * After expiration, must use refresh_token to obtain a new token.
     */
    public int expires_in;

    /**
     * Lifetime of the refresh token in seconds.
     * Used to obtain new access tokens without re-authenticating.
     */
    public int refresh_expires_in;

    /**
     * Refresh token for obtaining new access tokens after expiration.
     * Can be used instead of re-entering credentials.
     */
    public String refresh_token;

    /**
     * Type of token issued (typically "Bearer" for JWT tokens).
     * Used in Authorization header: {token_type} {access_token}
     */
    public String token_type;

    /**
     * Not-before policy timestamp.
     * Ensures token is not used before this time.
     */
    @JsonProperty("not-before-policy")
    public int notBeforePolicy;

    /**
     * Keycloak session state identifier.
     * Used for session tracking and logout.
     */
    public String session_state;

    /**
     * The scope/permissions granted to the client.
     * Defines what operations the client is authorized to perform.
     */
    public String scope;
}