package com.abcbank.shortcode.shortcode.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.utils.SlipGenerator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/shortcodes")
public class UtilController {

	@Value("${service.params.finquery.host}")
	private String finqueryHost;
	
	@Autowired
	ShortCodeRepo shortCodeRepo;
	
	@Autowired
	ShortCodeService shortCodeService;

	@GetMapping("/print/{shortCode}")
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
				data.put("sequenceNumber", shortCodeObj.getSequenceNumber());
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