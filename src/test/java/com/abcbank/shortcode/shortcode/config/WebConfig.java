package com.abcbank.shortcode.shortcode.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

class WebConfigTest {

    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        webConfig = new WebConfig();
    }

    @Test
    void shouldCreateInternalResourceViewResolverBean() {

        InternalResourceViewResolver resolver =
                webConfig.defaultViewResolver();

        assertNotNull(resolver);
    }

    @Test
    void shouldCreateRestTemplateBean() {

        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate restTemplate =
                webConfig.restTemplate(builder);

        assertNotNull(restTemplate);
    }

    @Test
    void shouldReturnDifferentRestTemplateInstances() {

        RestTemplateBuilder builder = new RestTemplateBuilder();

        RestTemplate first = webConfig.restTemplate(builder);
        RestTemplate second = webConfig.restTemplate(builder);

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
    }

    @Test
    void shouldReturnDifferentViewResolverInstances() {

        InternalResourceViewResolver first =
                webConfig.defaultViewResolver();

        InternalResourceViewResolver second =
                webConfig.defaultViewResolver();

        assertNotNull(first);
        assertNotNull(second);
        assertNotSame(first, second);
    }
}