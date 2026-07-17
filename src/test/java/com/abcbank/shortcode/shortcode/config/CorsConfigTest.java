package com.abcbank.shortcode.shortcode.config;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class CorsConfigTest {

    private CorsConfig corsConfig;
    private CorsConfigurationSource configurationSource;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        configurationSource = corsConfig.corsConfigurationSource();
    }

    @Test
    void shouldCreateCorsConfigurationSourceBean() {
        assertNotNull(configurationSource);
    }

    @Test
    void shouldConfigureAllowedOriginsCorrectly() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");

        CorsConfiguration configuration =
                configurationSource.getCorsConfiguration(request);

        assertNotNull(configuration);

        assertEquals(
                List.of(
                        "http://localhost:3000",
                        "http://10.60.30.*:3000"
                ),
                configuration.getAllowedOriginPatterns()
        );
    }

    @Test
    void shouldConfigureAllowedMethodsCorrectly() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/test");

        CorsConfiguration configuration =
                configurationSource.getCorsConfiguration(request);

        assertEquals(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                configuration.getAllowedMethods()
        );
    }

    @Test
    void shouldConfigureAllowedHeadersCorrectly() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        CorsConfiguration configuration =
                configurationSource.getCorsConfiguration(request);

        assertEquals(
                List.of("*"),
                configuration.getAllowedHeaders()
        );
    }

    @Test
    void shouldConfigureExposedHeadersCorrectly() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        CorsConfiguration configuration =
                configurationSource.getCorsConfiguration(request);

        assertEquals(
                List.of("*"),
                configuration.getExposedHeaders()
        );
    }

    @Test
    void shouldAllowCredentials() {

        MockHttpServletRequest request = new MockHttpServletRequest();

        CorsConfiguration configuration =
                configurationSource.getCorsConfiguration(request);

        assertTrue(configuration.getAllowCredentials());
    }
}