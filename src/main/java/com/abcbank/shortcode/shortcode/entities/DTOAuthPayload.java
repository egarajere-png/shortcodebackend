package com.abcbank.shortcode.shortcode.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Data Transfer Object for authentication request payload.
 * 
 * This DTO captures user credentials for authentication against Keycloak.
 * It's used in the /get-token endpoint to obtain OAuth 2.0 access tokens
 * for subsequent API requests.
 * 
 * Security Considerations:
 * - Passwords are transmitted in HTTP body (use HTTPS only)
 * - Should not be logged or cached
 * - Typically cleared from memory after authentication
 * - Should be transmitted only over encrypted HTTPS connections
 * 
 * Usage:
 * - POST /get-token endpoint: Request body contains username and password
 * - Forwarded to Keycloak for authentication
 * - Returns JWT access token on successful authentication
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class DTOAuthPayload {
    /** The user's login username (typically LDAP or Keycloak user ID) */
    String username;

    /** The user's password (transmitted only over HTTPS) */
    String password;
}
