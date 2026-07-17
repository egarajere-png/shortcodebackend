package com.abcbank.shortcode.shortcode.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

class OpenAPI30ConfigurationTest {

    private OpenAPI30Configuration configuration;

    @BeforeEach
    void setUp() {
        configuration = new OpenAPI30Configuration();
    }

    @Test
    void shouldCreateOpenApiBean() {

        OpenAPI openAPI = configuration.customizeOpenAPI();

        assertNotNull(openAPI);
    }

    @Test
    void shouldContainBearerSecurityRequirement() {

        OpenAPI openAPI = configuration.customizeOpenAPI();

        assertEquals(
                1,
                openAPI.getSecurity().size()
        );

        assertTrue(
                openAPI.getSecurity()
                        .get(0)
                        .containsKey("bearerAuth")
        );
    }

    @Test
    void shouldConfigureBearerAuthenticationScheme() {

        OpenAPI openAPI = configuration.customizeOpenAPI();

        SecurityScheme scheme =
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .get("bearerAuth");

        assertNotNull(scheme);

        assertEquals(
                SecurityScheme.Type.HTTP,
                scheme.getType()
        );

        assertEquals(
                "bearer",
                scheme.getScheme()
        );

        assertEquals(
                "JWT",
                scheme.getBearerFormat()
        );

        assertEquals(
                "bearerAuth",
                scheme.getName()
        );
    }

    @Test
    void shouldContainSecurityComponents() {

        OpenAPI openAPI = configuration.customizeOpenAPI();

        assertNotNull(openAPI.getComponents());

        assertNotNull(
                openAPI.getComponents().getSecuritySchemes()
        );

        assertTrue(
                openAPI.getComponents()
                        .getSecuritySchemes()
                        .containsKey("bearerAuth")
        );
    }
}