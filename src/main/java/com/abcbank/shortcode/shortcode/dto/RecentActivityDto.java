package com.abcbank.shortcode.shortcode.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {

    private Integer shortCode;

    private String accountNumber;

    private String action;

    private String performedBy;

    private String remarks;

    private LocalDateTime actionDate;
}