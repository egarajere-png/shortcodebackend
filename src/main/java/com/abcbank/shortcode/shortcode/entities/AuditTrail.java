package com.abcbank.shortcode.shortcode.entities;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class AuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer shortCodeId;

    private String accountNumber;

    private Integer shortCode;

    private String action;

    private String performedBy;

    private String remarks;

    private LocalDateTime actionDate;
}