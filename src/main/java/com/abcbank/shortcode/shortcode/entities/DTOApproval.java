package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

/**
 * DTO used when approving a pending shortcode request.
 */

@Data
public class DTOApproval {
   /**
    * Account number whose shortcode request is being approved.
    */
    private String accountNumber;

    /**
    * User approving the request.
    */
    private String approver;
}