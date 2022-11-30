package com.abcbank.shortcode.shortcode.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.DTOAccount;
import com.abcbank.shortcode.shortcode.entities.DTOApproval;
import com.abcbank.shortcode.shortcode.entities.DTOResponse;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.utils.Emailer;
import com.abcbank.shortcode.shortcode.utils.HTTPSClient;
import com.abcbank.shortcode.shortcode.utils.SlipGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class MainController {

	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	@Autowired
	ShortCodeService shortCodeService;

	public static void main(String[] args) {
		//System.out.println(new MainController().initiate("001190001000062"));
		System.out.println(new MainController().getAccount(350001));
	}

	@GetMapping("/shortcodes/api/validate/{accountNumber}")
	public DTOAccount validate(@PathVariable String accountNumber) {
		String url = "http://172.14.0.136:8081/account/" + accountNumber;
		String response = HTTPSClient.sendHttpsRequest(url, "", "get", new HashMap<>(), "text");
		JSONObject json = new JSONObject(response);
		DTOAccount account = new DTOAccount();
		account.setAccountName(json.getString("accountName"));
		account.setAccountNumber(json.getString("accountNumber"));
		account.setCustId(json.getString("custId"));
		account.setIdNumber(json.getString("idNumber"));
		account.setEmailAddress(json.getString("emailAddress"));
		account.setPhoneNumber(json.getString("phoneNum1"));
		account.setAccountStatus(json.getString("accountStatus"));
		return account;
	}

	/**
	 * 
	 * @param request 
	 * @return
	 */
	@PostMapping("/shortcodes/api/initiate")
	@ResponseBody
	public DTOResponse initiate(@RequestBody ShortCode request) {
		request.setDateInitiated(LocalDateTime.now());
		ShortCode shortCode = shortCodeRepo.findByAccountNumber(request.getAccountNumber());
		if(shortCode == null)
			shortCode = shortCodeRepo.save(request);

		DTOResponse response = new DTOResponse();
		if(shortCode.getId() > 0) {
			response.setStatusCode("000");
			response.setMessage("Short code request initiated successfully");
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not initiated, error occured");
		}
		return response;
	}

	@GetMapping("/shortcodes/api/pending")
	@ResponseBody
	public List<ShortCode> getPending() {
		List<ShortCode> shortCodeList = shortCodeRepo.findByApproved(false);
		return shortCodeList;
	}

	@PostMapping("/shortcodes/api/approve")
	@ResponseBody
	public ShortCode approve(@RequestBody DTOApproval request) {
		ShortCode shortCode = shortCodeRepo.findByAccountNumber(request.getAccountNumber());
		if(shortCode != null) {
			shortCode.setApprover(request.getApprover());
			String shortCodeValue = "35" + String.format("%04d", shortCode.getId());
			shortCode.setShortCode(Integer.parseInt(shortCodeValue));
			shortCode.setDateApproved(LocalDateTime.now());
			shortCode.setApproved(true);
			shortCode = shortCodeRepo.save(shortCode);
			
			String filePath = generateSlip(shortCode.getShortCode());
			log.info("File path: " + filePath);
			shortCodeService.sendReceiptEmail(shortCode);
		} else {
			shortCode = new ShortCode();
		}
		return shortCode;
	}

	@GetMapping("/shortcodes/print/{shortCode}")
	public ResponseEntity<Resource> downloadSlip(@PathVariable int shortCode) throws IOException {
		try {
			String fileName = shortCode + ".pdf";
			String filePath = generateSlip(shortCode);
			File file = new File(filePath);

			HttpHeaders header = new HttpHeaders();
			header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
			header.add("Cache-Control", "no-cache, no-store, must-revalidate");
			header.add("Pragma", "no-cache");
			header.add("Expires", "0");

			Path path = Paths.get(file.getAbsolutePath());
			ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

			return ResponseEntity.ok()
					.headers(header)
					.contentLength(file.length())
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.body(resource);
		} catch(Exception e) {
			return null;
		}
	}

	@GetMapping("/shortcodes/api/get-account/{shortCode}")
	public String getAccount(@PathVariable int shortCode) {
		try {
			return shortCodeRepo.findByShortCode(shortCode).getAccountNumber();
		} catch(Exception e) {
			return null;
		}
	}
	
	@GetMapping("/shortcodes/api/get-account-details/{shortCode}")
	public ShortCode getAccountDetails(@PathVariable int shortCode) {
		try {
			return shortCodeRepo.findByShortCode(shortCode);
		} catch(Exception e) {
			return new ShortCode();
		}
	}

	public String generateSlip(int shortCode) {
		try {
			String fileName = shortCode + ".pdf";
			String filePath = "/tmp/" + fileName;
			File file = new File(filePath);
			if(!file.exists()) {
				log.info("File does not exist, it has to be generated");
				ShortCode shortCodeObj = shortCodeRepo.findByShortCode(shortCode);
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("accountNumber", shortCodeObj.getAccountNumber());
				data.put("accountName", shortCodeObj.getAccountName());
				data.put("shortCode", shortCodeObj.getShortCode());
				SlipGenerator.generateShortCodeSlip(data);
			} else {
				log.info("File already exists");
			}
			return filePath;
		} catch(Exception e) {
			return null;
		}
	}
}