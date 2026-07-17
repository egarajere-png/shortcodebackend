package com.abcbank.shortcode.shortcode.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Jwt jwt) {

        JwtAuthenticationConverter converter =
                securityConfig.jwtAuthenticationConverter();

        return converter.convert(jwt).getAuthorities();
    }

    @Test
    void shouldCreateJwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
                securityConfig.jwtAuthenticationConverter();

        assertNotNull(converter);
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenRealmAccessIsMissing() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "user")
                .build();

        Collection<? extends GrantedAuthority> authorities =
                getAuthorities(jwt);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void shouldReturnMakerAuthority() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of("roles", List.of("maker"))
                )
                .build();

        Set<String> authorities =
                getAuthorities(jwt)
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("ROLE_maker"));
    }

    @Test
    void shouldReturnCheckerAuthority() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of("roles", List.of("checker"))
                )
                .build();

        Set<String> authorities =
                getAuthorities(jwt)
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("ROLE_checker"));
    }

    @Test
    void shouldGrantMakerAndCheckerWhenApiCallerRoleExists() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of("roles", List.of("apicaller"))
                )
                .build();

        Set<String> authorities =
                getAuthorities(jwt)
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertEquals(3, authorities.size());

        assertTrue(authorities.contains("ROLE_apicaller"));
        assertTrue(authorities.contains("ROLE_maker"));
        assertTrue(authorities.contains("ROLE_checker"));
    }

    @Test
    void shouldSupportMultipleRoles() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of(
                                "roles",
                                List.of("maker", "checker", "admin")
                        )
                )
                .build();

        Set<String> authorities =
                getAuthorities(jwt)
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertEquals(3, authorities.size());

        assertTrue(authorities.contains("ROLE_maker"));
        assertTrue(authorities.contains("ROLE_checker"));
        assertTrue(authorities.contains("ROLE_admin"));
    }

    @Test
    void shouldTreatApiCallerCaseInsensitively() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of("roles", List.of("ApiCaller"))
                )
                .build();

        Set<String> authorities =
                getAuthorities(jwt)
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

        assertTrue(authorities.contains("ROLE_ApiCaller"));
        assertTrue(authorities.contains("ROLE_maker"));
        assertTrue(authorities.contains("ROLE_checker"));
    }

    @Test
    void shouldReturnEmptyAuthoritiesWhenRolesListIsEmpty() {

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim(
                        "realm_access",
                        Map.of("roles", List.of())
                )
                .build();

        Collection<? extends GrantedAuthority> authorities =
                getAuthorities(jwt);

        assertTrue(authorities.isEmpty());
    }
}