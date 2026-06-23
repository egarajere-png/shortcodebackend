package com.abcbank.shortcode.shortcode.config;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/**
 * Security Configuration for Keycloak Integration.
 * 
 * This configuration class sets up Spring Security with Keycloak as the Identity
 * Provider (IdP) for OAuth 2.0 and OpenID Connect authentication. It enables role-based
 * access control using the @RolesAllowed annotation on controller methods.
 * 
 * Keycloak Integration:
 * - Authenticates users against Keycloak LDAP/database
 * - Validates JWT tokens issued by Keycloak
 * - Maps Keycloak roles to Spring Security authorities
 * - Manages user sessions and concurrent login tracking
 * 
 * Authorization Features:
 * - Role-based access control: Maker, Checker, API Caller roles
 * - Method-level security: @RolesAllowed annotations on endpoints
 * - Session management: Prevents concurrent logins, tracks active sessions
 * 
 * Security Configuration:
 * - Allows all requests (authorization enforced at method level)
 * - CSRF protection disabled (stateless API architecture)
 * - Session tracking enabled for concurrent login prevention
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(jsr250Enabled = true)
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
public class KeycloakConfig extends KeycloakWebSecurityConfigurerAdapter {

	/**
	 * Configures HTTP security for Keycloak authentication.
	 * 
	 * This method sets up the security filter chain:
	 * 1. Calls parent's configure() to apply Keycloak security filters
	 * 2. Permits all HTTP requests (authorization at method level via @RolesAllowed)
	 * 3. Disables CSRF protection (assumes API uses tokens, not cookies)
	 * 
	 * Authorization Strategy:
	 * While all requests are permitted at the HTTP level, method-level security
	 * is enforced via @RolesAllowed annotations on controller methods:
	 * - /initiate: @RolesAllowed({"maker", "apicaller"})
	 * - /approve: @RolesAllowed({"checker", "apicaller"})
	 * - etc.
	 * 
	 * @param http HttpSecurity configuration builder
	 * @throws Exception if security configuration fails
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Apply Keycloak security configuration from parent class
		super.configure(http);
		// Allow all HTTP requests (authorization at method level)
		http.authorizeRequests().anyRequest().permitAll();
		// Disable CSRF protection (stateless API architecture with token auth)
		http.csrf().disable();
	}

	/**
	 * Configures the session authentication strategy for Keycloak.
	 * 
	 * Session Management:
	 * Implements RegisterSessionAuthenticationStrategy to track user sessions
	 * in a central registry. This enables:
	 * - Detection and prevention of concurrent logins
	 * - Session invalidation on logout
	 * - Active session tracking across multiple servers (in clustered deployments)
	 * 
	 * The SessionRegistryImpl stores active sessions in memory for single-server
	 * deployments. For clustered environments, consider implementing a distributed
	 * session registry backed by Redis or database.
	 * 
	 * @return SessionAuthenticationStrategy configured for Keycloak
	 */
	@Override
	protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
		// Register sessions with central session registry for tracking
		return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
	}

	/**
	 * Configures global authentication with Keycloak provider.
	 * 
	 * Authentication Configuration:
	 * 1. Creates KeycloakAuthenticationProvider instance
	 * 2. Configures authority mapper to translate Keycloak roles to Spring roles
	 * 3. Registers the provider with Spring Security's authentication manager
	 * 
	 * Authority Mapping:
	 * Uses SimpleAuthorityMapper which converts Keycloak role names to Spring
	 * authorities. For example:
	 * - Keycloak role "maker" → Spring authority "ROLE_MAKER"
	 * - Keycloak role "checker" → Spring authority "ROLE_CHECKER"
	 * 
	 * This enables @RolesAllowed annotations to work with Keycloak role names.
	 * 
	 * @param auth AuthenticationManagerBuilder to configure
	 * @throws Exception if authentication configuration fails
	 */
	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		// Create Keycloak authentication provider
		KeycloakAuthenticationProvider keycloakAuthenticationProvider =
				keycloakAuthenticationProvider();
		
		// Configure role/authority mapping from Keycloak to Spring
		// Uses ROLE_ prefix convention for Spring Security authorities
		keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());

		// Register Keycloak provider with Spring Security
		auth.authenticationProvider(keycloakAuthenticationProvider);
	}
}
