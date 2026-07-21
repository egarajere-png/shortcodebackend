package com.abcbank.shortcode.shortcode.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;

/**
 * Unit tests for {@link JakartaMailSender}.
 *
 * <p>These tests verify the production implementation delegates to Jakarta Mail
 * without introducing any external dependencies or network calls.</p>
 */
class EmailerTest {

    private JakartaMailSender sender;

    @BeforeEach
    void setUp() {
        sender = new JakartaMailSender();
    }

    /*
     * ---------------------------------------------------------
     * Constructor and basic delegation tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should create a sender instance")
    void shouldCreateSenderInstance() {
        assertNotNull(sender);
    }

    @Test
    @DisplayName("Should send a message through Transport")
    void shouldSendMessageThroughTransport() throws MessagingException {
        Message message = createMessage();

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            assertDoesNotThrow(() -> sender.send(message));

            transport.verify(() -> Transport.send(message));
        }
    }

    /*
     * ---------------------------------------------------------
     * Failure and exception propagation tests
     * ---------------------------------------------------------
     */

    @Test
    @DisplayName("Should propagate MessagingException from Transport")
    void shouldPropagateMessagingExceptionFromTransport() throws Exception {
        Message message = createMessage();

        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send(message))
                    .thenThrow(new MessagingException("delivery failed"));

            MessagingException exception = assertThrows(
                    MessagingException.class,
                    () -> sender.send(message));

            assertNotNull(exception);
            transport.verify(() -> Transport.send(message));
        }
    }

    @Test
    @DisplayName("Should throw when the supplied message is null")
    void shouldThrowWhenMessageIsNull() {
        try (MockedStatic<Transport> transport = mockStatic(Transport.class)) {
            transport.when(() -> Transport.send((Message) null))
                    .thenThrow(new NullPointerException("message must not be null"));

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> sender.send(null));

            assertNotNull(exception);
            transport.verify(() -> Transport.send((Message) null));
        }
    }

    private Message createMessage() {
        return mock(Message.class);
    }
}
