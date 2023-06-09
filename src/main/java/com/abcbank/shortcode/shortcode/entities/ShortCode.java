package com.abcbank.shortcode.shortcode.entities;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
@Entity
public class ShortCode {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
	private int shortCode;
	private int sequenceNumber;
	private LocalDateTime dateInitiated;
	private LocalDateTime dateApproved;
	private boolean approved = false;
}
