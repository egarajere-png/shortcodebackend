package com.abcbank.shortcode.shortcode.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public InternalResourceViewResolver defaultViewResolver() {
		return new InternalResourceViewResolver();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/shortcodes/**").allowedOriginPatterns("*");
	}
}