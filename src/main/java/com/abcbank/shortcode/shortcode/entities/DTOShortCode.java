package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * Indicates whether a preferred shortcode is available.
 */

@Data
public class DTOShortCode {
    /**
    * Customer account number.
    */
    private String accountNumber;

    /**
    * Reason for deletion.
    */

    private String deleteRemark;

    /**
    * Shortcode to be deleted.
    */
   
    private int shortCode;
}