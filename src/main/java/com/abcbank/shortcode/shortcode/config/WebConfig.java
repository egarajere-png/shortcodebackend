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
 * Provides common web configuration
 * and shared application beans.
 */

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	/**
 * Creates a shared RestTemplate instance
 * for external HTTP communication.
 */
	@Bean
	public InternalResourceViewResolver defaultViewResolver() {
		return new InternalResourceViewResolver();
	}

	/**
 * Creates a shared RestTemplate instance
 * for external HTTP communication.
 */
	@Bean 
	public RestTemplate restTemplate(RestTemplateBuilder builder){
	  return builder.build();
	}
	
	/**
	 * Configures Cross-Origin Resource Sharing (CORS) policy.
	 * 
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Allow cross-origin requests to all /shortcodes/** endpoints from any origin
		registry.addMapping("/shortcodes/**").allowedOriginPatterns("*");
	}
}