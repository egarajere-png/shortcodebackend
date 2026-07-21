package com.abcbank.shortcode.shortcode.utils;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Component
@Slf4j
public class Emailer {
	
	@Value("${service.params.smtphost}")
	private String smtpHost;

	private final MailSender mailSender;

    public Emailer(MailSender mailSender) {
        this.mailSender = mailSender;
    }
	
	public boolean send(String from, String to, String subject, String body) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		Session session = Session.getDefaultInstance(properties, null);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			
			for(String rcpt : to.split(",")) {
				System.out.println("To: " + rcpt);
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
			}
			
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setText(body, "utf-8", "html");
			mailSender.send(message);
			System.out.println("message sent successfully...");
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
			return false;
		}
	}
	
	public boolean send(String from, String to, String cc, String subject, String body) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		Session session = Session.getDefaultInstance(properties, null);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			
			for(String rcpt : to.split(",")) {
				System.out.println("To: " + rcpt);
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
			}
			
			for(String rcpt : cc.split(",")) {
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(rcpt));
			}
			
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject(subject);
			message.setText(body, "utf-8", "html");
			mailSender.send(message);
			System.out.println("message sent successfully...");
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
			return false;
		}
	}
	
	public boolean send(String from, String[] to, String[] cc, String subject, String body, String[] files) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		Session session = Session.getDefaultInstance(properties, null);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			for(String rcpt : to) {
				log.info("To: {}", rcpt);
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
			}
			
			for(String rcpt2 : cc) {
				log.info("Cc: {}", rcpt2);
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(rcpt2));
			}
			
			message.setSubject(subject);
			
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        
	        for(String file : files) {
		        messageBodyPart = new MimeBodyPart();
		        DataSource source = new FileDataSource(file);
		        messageBodyPart.setDataHandler(new DataHandler(source));
		        messageBodyPart.setFileName(file.substring(file.lastIndexOf("/") + 1,file.length()));
		        multipart.addBodyPart(messageBodyPart);
	        }
            message.setContent(multipart);
            
			mailSender.send(message);
			
			log.info("Message successfully sent");
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 
	 * @param from
	 * @param to coma separated for more than one
	 * @param cc coma separated for more than one 
	 * @param subject
	 * @param body
	 * @param files coma separated for more than one
	 * @return
	 */
	public boolean send(String from, String to, String cc, String subject, String body, String files) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		Session session = Session.getDefaultInstance(properties, null);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			for(String rcpt : to.split(",")) {
				log.info("To: {}", rcpt);
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(rcpt));
			}
			
			for(String rcpt2 : cc.split(",")) {
				log.info("Cc: {}", rcpt2);
				if(rcpt2 != null && rcpt2 != "")
				    message.addRecipient(Message.RecipientType.CC, new InternetAddress(rcpt2));
			}
			message.setSubject(subject);
			
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			
	        Multipart multipart = new MimeMultipart();
	        multipart.addBodyPart(messageBodyPart);
	        
	        for(String file : files.split(",")) {
		        messageBodyPart = new MimeBodyPart();
		        DataSource source = new FileDataSource(file);
		        messageBodyPart.setDataHandler(new DataHandler(source));
		        messageBodyPart.setFileName(file.substring(file.lastIndexOf("/") + 1,file.length()));
		        multipart.addBodyPart(messageBodyPart);
	        }
            message.setContent(multipart);
            
			mailSender.send(message);
			
			log.info("Message successfully sent");
			return true;
		} catch (MessagingException mex) {
			mex.printStackTrace();
			return false;
		}
	}
}