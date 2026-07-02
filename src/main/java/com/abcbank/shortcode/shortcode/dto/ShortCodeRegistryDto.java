package com.abcbank.shortcode.shortcode.dto;

import lombok.Data;

@Data
public class ShortCodeRegistryDto {

    private Integer id;

    private Integer shortCode;

    private String accountNumber;

    private String accountName;

    private String phoneNumber;

    private String emailAddress;

    private boolean approved;

    private boolean deleted;

    private String initiator;

    private String approver;

    private String dateInitiated;

    private String dateApproved;

    private boolean deleteInitiated;

    private String status;

}