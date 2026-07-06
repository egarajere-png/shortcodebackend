package com.abcbank.shortcode.shortcode;

// import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

// import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

/**
 * Main Spring Boot application class for the Short Code Generation System.
 * 
 * This application provides a comprehensive banking platform for managing short codes (USSD codes)
 * used in Mpesa paybill transactions. It implements a Maker-Checker workflow for secure
 * short code creation, approval, and deletion operations, with full audit trail logging.
 * 
 * Key Features:
 * - Keycloak-based authentication and authorization
 * - Encrypted property support for sensitive configuration data
 * - Role-based access control (Maker, Checker, API Caller roles)
 * - Comprehensive audit trail for all operations
 * - Hash-based integrity validation for short code records
 * - Finacle core banking system integration
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@SpringBootApplication
// @EnableEncryptableProperties
public class ShortcodeApplication {

	/**
	 * Application entry point for the Spring Boot Short Code application.
	 * Initializes the application context and starts the embedded web server.
	 * 
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(ShortcodeApplication.class, args);
	}

	/**
	 * Configures Keycloak Spring Boot integration.
	 * This bean enables Keycloak authentication provider to resolve configuration
	 * from the application properties file, supporting Keycloak security features
	 * such as role-based access control and token validation.
	 * 
	 * @return KeycloakSpringBootConfigResolver instance for Keycloak configuration resolution
	 */
	// @Bean
// 	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
// 	    return new KeycloakSpringBootConfigResolver();
// 	}
}
