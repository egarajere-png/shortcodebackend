package com.abcbank.shortcode.shortcode.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.abcbank.shortcode.shortcode.entities.DTOAuthPayload;
import com.abcbank.shortcode.shortcode.entities.DTOAuthPayloadResponse;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AuthController {

	@Value("${keycloak.config.token-url}")
    String KEYCLOAK_URL;
	@Value("${keycloak.config.client-id}")
	private String kcClientId;
	
	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	@Autowired
	ShortCodeService shortCodeService;

	@PostMapping("/shortcodes/api/get-token")
    public DTOAuthPayloadResponse authenticateUser(@RequestBody DTOAuthPayload authPayload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", kcClientId);
        map.add("username", authPayload.getUsername());
        map.add("password", authPayload.getPassword());
        map.add("grant_type", "password");
        log.info("\n\n ============= : {}" + map);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        try {
            return new RestTemplate().exchange(KEYCLOAK_URL,
                    HttpMethod.POST,
                    entity,
                    DTOAuthPayloadResponse.class
            ).getBody();
        } catch (Exception exception) {
            log.info(exception.getLocalizedMessage());
            String responseError = exception.getLocalizedMessage().replace("400 Bad Request: ", "");
            log.error(responseError);
            return new DTOAuthPayloadResponse();
        }
    }
}