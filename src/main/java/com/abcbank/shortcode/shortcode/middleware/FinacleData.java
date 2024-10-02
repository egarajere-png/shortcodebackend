package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FinacleData {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${service.params.finquery.host}")
	private String finqueryHost;

	public String fetchCBSShortCode(String accountNumber) {
		String endpoint = "http://" + finqueryHost + "/api/finacle/short-code/" + accountNumber;
		ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
		return response.getBody();
	}
}
