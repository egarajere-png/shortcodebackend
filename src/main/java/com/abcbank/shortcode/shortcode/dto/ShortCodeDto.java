package com.abcbank.shortcode.shortcode.dto;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class ShortCodeDto {

    private int id;
	private String initiator;
	private String approver;
	private String accountNumber;
	private String accountName;
	private String phoneNumber;
	private String emailAddress;
	private String idNumber;
	private String custId;
	private String remark;
	private String deleteRemark;
	private int shortCode;
	private int sequenceNumber;
	private String dateInitiated;
	private String dateApproved;
	private boolean approved = false;
	private boolean deleteInitiated = false;
	private boolean deleted = false;
}
