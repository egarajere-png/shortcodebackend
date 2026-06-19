package com.abcbank.shortcode.shortcode.dto;

import lombok.Data;

@Data
public class AuditTrailDto {

    private String action;

    private String performedBy;

    private String remarks;

    private String actionDate;
}