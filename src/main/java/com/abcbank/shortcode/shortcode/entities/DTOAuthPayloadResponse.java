package com.abcbank.shortcode.shortcode.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * DTO representing the authentication response returned by Keycloak.
 *
 * Contains the access token, refresh token, expiry information,
 * and granted scopes.
 */

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOAuthPayloadResponse {
    /**
     * JWT access token for authorizing API requests.
     */
    public String access_token;

    /**
     * Lifetime of the access token in seconds.
     */
    public int expires_in;

    /**
     * Lifetime of the refresh token in seconds.
     */
    public int refresh_expires_in;

    /**
     * Refresh token for obtaining new access tokens after expiration.
     */
    public String refresh_token;

    /**
     * Type of token issued (typically "Bearer" for JWT tokens).
     */
    public String token_type;

    /**
     * Not-before policy timestamp.
     */
    @JsonProperty("not-before-policy")
    public int notBeforePolicy;

    /**
     * Keycloak session state identifier.
     * Used for session tracking and logout.
    public String session_state;

    /**
     * The scope/permissions granted to the client.
     */
    public String scope;
}