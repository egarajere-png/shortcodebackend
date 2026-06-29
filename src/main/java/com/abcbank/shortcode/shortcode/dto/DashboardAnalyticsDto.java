package com.abcbank.shortcode.shortcode.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAnalyticsDto{

    private long totalShortcodes;

    private long activeShortcodes;

    private long pendingApprovals;

    private long pendingDeletions;

    private long deletedShortcodes;

    private double approvalRate;

    

}
