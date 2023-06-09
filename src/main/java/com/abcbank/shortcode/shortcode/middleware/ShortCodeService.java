package com.abcbank.shortcode.shortcode.middleware;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.abcbank.shortcode.shortcode.entities.ShortCode;
import com.abcbank.shortcode.shortcode.utils.Emailer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ShortCodeService {
	
	@Autowired
	private Emailer emailer;
	
	public void sendReceiptEmail(ShortCode shortCode) {
		
		log.info("\nSending slip for eslip {}", shortCode.getShortCode());

		String emailAddress = shortCode.getEmailAddress();
		String customerName = shortCode.getAccountName();
		
		log.info("\n\nAbout to emailAddress: " + emailAddress);
		String from = "ABC Bank Support<talk2us@abcthebank.com>";
		String subject = "ABC Bank - New Short-code " + shortCode.getShortCode();
		String body = "Dear " + customerName + ",\n\n" + 
					"Your ABC Bank-Mpesa short-code, " + shortCode.getShortCode() + " has been generated, kindly download the attached slip for your record." +
					"\nYou can share it so others can send you money with this short-code as the account for Mpesa paybill." +
					"\n\nABC Bank Team";
		emailer.send(from, emailAddress.toLowerCase(), "", subject, body, "/tmp/" + shortCode.getShortCode() + ".pdf");
		log.info("\n\nReceipt has been sent on email");
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
}