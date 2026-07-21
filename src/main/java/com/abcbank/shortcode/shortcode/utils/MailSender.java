package com.abcbank.shortcode.shortcode.utils;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

/**
 * Sends email messages.
 *
 * This abstraction allows Emailer to be unit tested by
 * mocking the mail sender instead of calling the static
 * Jakarta Mail Transport class directly.
 */
public interface MailSender {

    /**
     * Sends the supplied email message.
     *
     * @param message email message
     * @throws MessagingException if sending fails
     */
    void send(Message message) throws MessagingException;

}