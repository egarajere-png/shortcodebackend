package com.abcbank.shortcode.shortcode.config;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.core.convert.converter.Converter;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import org.springframework.http.HttpMethod;


/**
 * Configures application security,
 * JWT authentication, and role-based access.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {
   
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())

        .authorizeHttpRequests(auth -> auth

            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            .requestMatchers(
                    "/shortcodes/api/**",
                    "/public/api/v1/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**"
            ).permitAll()

            .anyRequest().authenticated()
        )

        .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
        );

    return http.build();
}

    public JwtAuthenticationConverter jwtAuthenticationConverter() {

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(jwt -> {

        Collection<GrantedAuthority> authorities = new ArrayList<>();

        var realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess != null && realmAccess.containsKey("roles")) {

            var roles = (Collection<String>) realmAccess.get("roles");

            roles.forEach(role -> {

                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

                if ("apicaller".equalsIgnoreCase(role)) {

                    authorities.add(new SimpleGrantedAuthority("ROLE_maker"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_checker"));

                }

            });

        }

        return authorities;

    });

    return converter;
}
}

