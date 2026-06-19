package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.utils.Emailer;
import com.abcbank.shortcode.shortcode.utils.Hashing;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShortCodeService {
	
	@Autowired
	private Emailer emailer;

	@Autowired
	private Hashing hashing;
	
	public void sendReceiptEmail(ShortCode shortCode) {
		
		log.info("Sending slip for eslip {}", shortCode.getShortCode());

		String emailAddress = shortCode.getEmailAddress();
		String customerName = shortCode.getAccountName();
		
		log.info("About to emailAddress: " + emailAddress);
		String from = "ABC Bank Support<talk2us@abcthebank.com>";
		String subject = "ABC Bank - New Short-code " + shortCode.getShortCode();
		String body = "Dear " + customerName + ",\n\n" + 
					"Your ABC Bank-Mpesa short-code, " + shortCode.getShortCode() + " has been generated, kindly download the attached slip for your record." +
					"\nYou can share it so others can send you money with this short-code as the account for Mpesa paybill." +
					"\n\nABC Bank Team";
		emailer.send(from, emailAddress.toLowerCase(), "", subject, body, "/tmp/" + shortCode.getShortCode() + ".pdf");
		log.info("Receipt has been sent on email");
	}
	
	public boolean validateRequest(ShortCode shortCode) {
		if(shortCode.getAccountName() == null
				|| shortCode.getAccountNumber() == null
				|| shortCode.getIdNumber() == null
				|| shortCode.getInitiator() == null
				|| shortCode.getCustId() == null) {
			return false;
		} else {
			return true;
		}
	}
public String generateHash(ShortCode shortCode) {

    String data = String.join("|",
            String.valueOf(shortCode.getId()),
            safe(shortCode.getAccountNumber()),
            safe(shortCode.getCustId()),
            safe(shortCode.getAccountName()),
            safe(shortCode.getIdNumber()),
            safe(shortCode.getEmailAddress()),
            safe(shortCode.getPhoneNumber()),
            String.valueOf(shortCode.getShortCode()),
            String.valueOf(shortCode.isApproved()),
            String.valueOf(shortCode.isDeleted())
    );

    log.info("Hash source data: {}", data);

    return hashing.hash256(data);
}

private String safe(String value) {
    return value == null ? "" : value.trim();
}
}