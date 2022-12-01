package com.abcbank.shortcode.shortcode;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ShortcodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortcodeApplication.class, args);
	}

	@Bean
	public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
	    return new KeycloakSpringBootConfigResolver();
	}
}
