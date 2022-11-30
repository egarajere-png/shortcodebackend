package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

@Data
public class DTOAccount {
    private String accountNumber;
    private String accountName;
    private String custId;
    private String idNumber;
    private String phoneNumber;
    private String emailAddress;
    private String accountStatus;
}
