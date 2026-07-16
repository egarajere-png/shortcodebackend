package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;


/**
 * Service responsible for communication with
 * the Finacle Core Banking System.
 *
 * Provides account and shortcode lookup operations.
 */
@Service
public class FinacleData {

	/**
	 * RestTemplate bean for making HTTP requests to Finacle endpoints.
	 * Provides simplified HTTP communication with automatic serialization/deserialization.
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
 * Retrieves account information from Finacle.
 *
 * @param accountNumber customer account number
 * @return account details as JSON.
 */
	@Value("${service.params.finquery.host}")
	private String finqueryHost;

	/**
 * Retrieves the shortcode associated with
 * the supplied account from Finacle.
 *
 * @param accountNumber customer account number
 * @return shortcode registered in Finacle.
 */
	public String fetchCBSShortCode(String accountNumber) {
		// Build Finacle endpoint URL using configured host
		String endpoint = "http://" + finqueryHost + "/api/finacle/short-code/" + accountNumber;
		
		// Make HTTP GET request to Finacle and retrieve response as String
		ResponseEntity<String> response = restTemplate.getForEntity(endpoint, String.class);
		
		// Return the short code value from the response body
		return response.getBody();
	}

	public JSONObject fetchAccount(String accountNumber) {

    String endpoint =
            "http://" + finqueryHost +
            "/api/finacle/account-data/" +
            accountNumber;

    ResponseEntity<String> response =
            restTemplate.getForEntity(endpoint, String.class);

    return new JSONObject(response.getBody());
}
}
