package com.abcbank.shortcode.shortcode.entities;

import lombok.Data;

@Data
public class DTOShortCode {
	private String accountNumber;
	private String deleteRemark;
    private int shortCode;
}