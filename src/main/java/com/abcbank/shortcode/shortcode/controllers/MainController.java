package com.abcbank.shortcode.shortcode.controllers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.entities.DTOAccount;
import com.abcbank.shortcode.shortcode.entities.DTOApproval;
import com.abcbank.shortcode.shortcode.entities.DTOResponse;
import com.abcbank.shortcode.shortcode.entities.DTOShortCode;
import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.middleware.ShortCodeService;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;
import com.abcbank.shortcode.shortcode.utils.HTTPSClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/shortcodes/api")
public class MainController {

	@Value("${service.params.finquery.host}")
	private String finqueryHost;

	@Autowired
	ShortCodeRepo shortCodeRepo;

	@Autowired
	ShortCodeService shortCodeService;

	@GetMapping("/validate/{accountNumber}")
	@RolesAllowed({ "apicaller", "maker", "checker" })
	public DTOAccount validate(@PathVariable String accountNumber) {
		String url = "http://" + finqueryHost + "/api/finacle/account-data/" + accountNumber;
		String response = HTTPSClient.sendHttpsRequest(url, "", "get", new HashMap<>(), "text");
		JSONObject json = new JSONObject(response);
		String idNumber = null;
		String passPortNumber = null;
		try {idNumber = json.getString("idNumber");} catch(Exception e) {}
		try {passPortNumber = json.getString("ppNumber");} catch(Exception e) {}
		String idOrPasspord = idNumber != null ? idNumber : passPortNumber != null ? passPortNumber : "None";
		DTOAccount account = new DTOAccount();
		account.setAccountName(json.getString("accountName"));
		account.setAccountNumber(json.getString("accountNumber"));
		account.setCustId(json.getString("custId"));
		account.setIdNumber(idOrPasspord);
		account.setEmailAddress(json.getString("emailAddress"));
		account.setPhoneNumber(json.getString("phoneNumber"));
		account.setAccountStatus(json.getString("status"));
		return account;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/initiate")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse initiate(@RequestBody ShortCode request) {
		log.info(" ========== About to initiate short code reqeust, account number: {}, name: {}",
				request.getAccountNumber(), request.getAccountName());
		List<ShortCode> list = shortCodeRepo.findByAccountNumberAndApproved(request.getAccountNumber(), true);
		DTOResponse response = new DTOResponse();
		if (list.size() > 0) {
			int shortCodeValue = list.get(0).getShortCode();
			log.info(" ================ Account existing, short code: {}", shortCodeValue);
			response.setStatusCode("103");
			response.setMessage("Shortcode is already granted for the account");
			return response;
		}
		request.setApproved(false);

		if (shortCodeService.validateRequest(request) == false) {
			response.setStatusCode("104");
			response.setMessage("Some details are missing in the request");
			return response;
		}

		List<ShortCode> shortCodeList = shortCodeRepo.findByAccountNumberAndApproved(request.getAccountNumber(), false);

		if (shortCodeList.size() > 0) {
			response.setStatusCode("101");
			response.setMessage("There is a short code request for this account pending approval");
			return response;
		}
		request.setDateInitiated(LocalDateTime.now());

		ShortCode shortCode = shortCodeRepo.save(request);

		if (shortCode.getId() > 0) {
			String hash = shortCodeService.generateHash(shortCode);
			shortCode.setHash(hash);
			shortCodeRepo.save(request);
			response.setStatusCode("000");
			response.setMessage("Short code request initiated successfully");
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not initiated, error occured");
		}
		return response;
	}

	@PostMapping("/approve")
	@RolesAllowed({ "apicaller", "checker" })
	@ResponseBody
	public DTOResponse approve(@RequestBody DTOApproval request) {
		List<ShortCode> shortCodeList = shortCodeRepo.findByAccountNumberOrderByIdDesc(request.getAccountNumber());
		int count = shortCodeList.size();
		ShortCode shortCode = new ShortCode();
		DTOResponse response = new DTOResponse();
		if (count > 0) {
			shortCode = shortCodeList.get(0);
			String generatedHash = shortCodeService.generateHash(shortCode);
			if (!generatedHash.equals(shortCode.getHash())) {
				response.setStatusCode("104");
				response.setMessage("Alarm: failed integrity check!");
				return response;
			}
			shortCode.setSequenceNumber(count);
			shortCode.setApprover(request.getApprover());
			String shortCodeValue = "35" + String.format("%04d", shortCode.getId());
			shortCode.setShortCode(Integer.parseInt(shortCodeValue));
			shortCode.setDateApproved(LocalDateTime.now());
			shortCode.setApproved(true);
			generatedHash = shortCodeService.generateHash(shortCode);
			shortCode.setHash(generatedHash);
			shortCode = shortCodeRepo.save(shortCode);

			String filePath = new UtilController().generateSlip(shortCode.getShortCode());
			log.info("File path: " + filePath);
			shortCodeService.sendReceiptEmail(shortCode);
			response.setStatusCode("000");
			response.setShortCode(shortCode.getShortCode());
			response.setMessage("Shortcode successfully generated");
		}
		return response;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	@DeleteMapping("/delete")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse delete(@RequestBody DTOShortCode request) {
		DTOResponse response = new DTOResponse();
		ShortCode shortCode = shortCodeRepo.findByShortCode(request.getShortCode());
		if (shortCode == null) {
			response.setStatusCode("104");
			response.setMessage("Shortcode does not exist!");
			return response;
		}
		if (!shortCode.getAccountNumber().equalsIgnoreCase(request.getAccountNumber())) {
			response.setStatusCode("104");
			response.setMessage("Something wrong with the request");
			return response;
		}

		shortCode.setDeleteInitiated(true);
		shortCode.setDeleteRemark(request.getDeleteRemark());
		response.setMessage("Short code delete initiated successfully, pending approval");

		if (shortCode.getId() > 0) {
			response.setStatusCode("000");
			shortCodeRepo.save(shortCode);
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not completed, error occured");
		}
		return response;
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	@PostMapping("/approve-delete")
	@RolesAllowed({ "apicaller", "maker" })
	@ResponseBody
	public DTOResponse approveDelete(@RequestBody DTOShortCode request) {
		DTOResponse response = new DTOResponse();
		ShortCode shortCode = shortCodeRepo.findByShortCode(request.getShortCode());
		if (shortCode == null) {
			response.setStatusCode("104");
			response.setMessage("Shortcode does not exist!");
			return response;
		}
		if (!shortCode.getAccountNumber().equalsIgnoreCase(request.getAccountNumber())) {
			response.setStatusCode("104");
			response.setMessage("Something wrong with the request");
			return response;
		}
		shortCode.setDeleteInitiated(false);
		shortCode.setDeleted(true);
		response.setMessage("Short code has been deleted from the system");

		if (shortCode.getId() > 0) {
			response.setStatusCode("000");
			shortCodeRepo.save(shortCode);
		} else {
			response.setStatusCode("104");
			response.setMessage("Request not completed, error occured");
		}
		return response;
	}

	@GetMapping("/pending")
	@ResponseBody
	public List<ShortCode> getPending() {
		List<ShortCode> shortCodeList = shortCodeRepo.findByApproved(false);
		return shortCodeList;
	}

	@GetMapping("/pending-delete")
	@ResponseBody
	public List<ShortCode> getPendingDelete() {
		List<ShortCode> shortCodeList = shortCodeRepo.findByDeleteInitiatedAndDeleted(true, false);
		log.info(" ===================== Pending delete: {}", shortCodeList);
		return shortCodeList;
	}

	@GetMapping("/get-shortcodes/{accountNumber}")
	@ResponseBody
	public List<ShortCode> getPending(@PathVariable String accountNumber) {
		List<ShortCode> shortCodeList = shortCodeRepo.findByAccountNumberOrderByIdDesc(accountNumber);
		return shortCodeList;
	}

	@GetMapping("/get-account/{shortCodeNumber}")
	public String getAccount(@PathVariable int shortCodeNumber) {
		try {
			ShortCode shortCode = shortCodeRepo.findByShortCode(shortCodeNumber);
			if (shortCode == null)
				return null;
			log.info("================ shortCode: {}", shortCode);
			String generatedHash = shortCodeService.generateHash(shortCode);
			String storedHash = shortCode.getHash();
			log.info("================= StoredHash: {}, GeneratedHash: {}", storedHash, generatedHash);
			return (generatedHash.equals(storedHash))
					? shortCode.isDeleted() == false ? shortCode.getAccountNumber() : null
					: null;
		} catch (Exception e) {
			return null;
		}
	}

	@GetMapping("/get-account-details/{shortCode}")
	public ShortCode getAccountDetails(@PathVariable int shortCode) {
		try {
			return shortCodeRepo.findByShortCode(shortCode);
		} catch (Exception e) {
			return new ShortCode();
		}
	}
}