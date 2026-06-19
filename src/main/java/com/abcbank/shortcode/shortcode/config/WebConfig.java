package com.abcbank.shortcode.shortcode.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Web Application Configuration for Spring MVC and Cross-Origin Request Handling.
 * 
 * This configuration class provides:
 * - Bean definitions for HTTP clients and view resolution
 * - Cross-Origin Resource Sharing (CORS) policy configuration
 * - Default view resolver for template-based responses
 * 
 * Key Features:
 * - RestTemplate bean for external HTTP communication (e.g., Finacle, Keycloak)
 * - CORS configuration allowing cross-domain requests for web client applications
 * - View resolver for JSP/template rendering (if needed)
 * 
 * @author ABC Bank Development Team
 * @version 1.0
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	/**
	 * Creates a default view resolver for template-based responses.
	 * 
	 * View Resolution:
	 * This resolver maps logical view names to physical view resources.
	 * Example:
	 * - Return "success" → Renders /WEB-INF/success.jsp
	 * 
	 * Note: The Short Code application primarily uses REST APIs with JSON responses,
	 * so this resolver is rarely used. It's configured for completeness and potential
	 * future JSP template usage.
	 * 
	 * @return InternalResourceViewResolver configured for JSP/template files
	 */
	@Bean
	public InternalResourceViewResolver defaultViewResolver() {
		return new InternalResourceViewResolver();
	}

	/**
	 * Creates a RestTemplate bean for external HTTP communication.
	 * 
	 * RestTemplate Usage:
	 * Used throughout the application for HTTP requests:
	 * - FinacleData service: Queries Finacle core banking system
	 * - AuthController: Communicates with Keycloak token endpoint
	 * - HTTPSClient: Additional HTTPS requests for external integrations
	 * 
	 * Bean Configuration:
	 * Uses Spring Boot's RestTemplateBuilder for intelligent defaults and
	 * easy customization (timeouts, interceptors, error handlers, etc.)
	 * 
	 * Future Enhancement:
	 * Can be extended with RestTemplateBuilder to add:
	 * - Connection/read timeouts
	 * - Custom interceptors for logging or authentication
	 * - Error handling and retry policies
	 * - HTTP client pooling and connection reuse
	 * 
	 * @param builder RestTemplateBuilder for configuring the template
	 * @return RestTemplate bean for HTTP communication
	 */
	@Bean 
	public RestTemplate restTemplate(RestTemplateBuilder builder){
	  return builder.build();
	}
	
	/**
	 * Configures Cross-Origin Resource Sharing (CORS) policy.
	 * 
	 * CORS Configuration:
	 * Enables the API to accept requests from web clients running on different
	 * origins (different domains, ports, protocols). This is essential for:
	 * - Web client applications (SPA, dashboard)
	 * - Third-party integrations
	 * - Mobile applications with embedded web views
	 * 
	 * Current Configuration:
	 * - Path: /shortcodes/** (all short code API endpoints)
	 * - Allowed Origins: * (any origin)
	 * 
	 * Security Considerations:
	 * The wildcard origin "*" allows requests from ANY origin. For production
	 * environments, this should be restricted to:
	 * - Specific domain: allowedOrigins("https://app.example.com")
	 * - Multiple domains: allowedOrigins("https://app.example.com", "https://admin.example.com")
	 * 
	 * The allowedOriginPatterns("*") with credentials=false is acceptable for
	 * stateless APIs using token-based authentication. For cookie-based sessions,
	 * explicitly list allowed origins.
	 * 
	 * @param registry CorsRegistry to configure CORS mappings
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Allow cross-origin requests to all /shortcodes/** endpoints from any origin
		registry.addMapping("/shortcodes/**").allowedOriginPatterns("*");
	}
}