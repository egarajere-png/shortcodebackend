package com.abcbank.shortcode.shortcode.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

    /**
    * DTO containing user credentials used for authentication.
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
