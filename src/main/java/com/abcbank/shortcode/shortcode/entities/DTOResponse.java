package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Generic API response returned after shortcode operations.
 */

@Data
public class DTOResponse {
    /**
    * Response status code.
    */
    private String statusCode;

    /**
    * Generated shortcode.
    */
    private int shortCode;

    /**
    * Response message.
    */
    private String message;

    /**
    * Indicates whether a preferred shortcode is available.
    */

    private boolean available;
}
