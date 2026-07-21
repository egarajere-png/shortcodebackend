package com.abcbank.shortcode.shortcode.utils;

import org.springframework.stereotype.Component;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

/**
 * Production implementation that delegates
 * to Jakarta Mail Transport.
 */
@Component
public class JakartaMailSender implements MailSender {

    @Override
    public void send(Message message) throws MessagingException {
        Transport.send(message);
    }

}