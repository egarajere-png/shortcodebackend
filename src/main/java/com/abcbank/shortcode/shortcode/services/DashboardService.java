package com.abcbank.shortcode.shortcode.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abcbank.shortcode.shortcode.dto.DashboardAnalyticsDto;
import com.abcbank.shortcode.shortcode.repo.AuditTrailRepo;
import com.abcbank.shortcode.shortcode.repo.ShortCodeRepo;

import java.util.List;
import java.util.stream.Collectors;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.springframework.data.domain.PageRequest;



import com.abcbank.shortcode.shortcode.dto.RecentActivityDto;
import com.abcbank.shortcode.shortcode.dto.WeeklySummaryDto;
import com.abcbank.shortcode.shortcode.entities.AuditTrail;

@Service
public class DashboardService {

    @Autowired
    private ShortCodeRepo shortCodeRepo;

    @Autowired
    private AuditTrailRepo auditTrailRepo;

    public DashboardAnalyticsDto getAnalytics() {

        long total = shortCodeRepo.count();

        long active = shortCodeRepo.countByApprovedAndDeleted(true, false);

        long pending = shortCodeRepo.countByApproved(false);

        long pendingDelete =
                shortCodeRepo.countByDeleteInitiatedAndDeleted(true, false);

        long deleted = shortCodeRepo.countByDeleted(true);

        long approved = shortCodeRepo.countByApproved(true);

        double approvalRate = total == 0
                ? 0
                : ((double) approved / total) * 100;

        return DashboardAnalyticsDto.builder()
                .totalShortcodes(total)
                .activeShortcodes(active)
                .pendingApprovals(pending)
                .pendingDeletions(pendingDelete)
                .deletedShortcodes(deleted)
                .approvalRate(Math.round(approvalRate * 100.0) / 100.0)
                .build();
    }

    public List<RecentActivityDto> getRecentActivity() {

    List<AuditTrail> activities =
            auditTrailRepo.findAllByOrderByActionDateDesc(PageRequest.of(0, 10));

    return activities.stream()
            .map(activity -> RecentActivityDto.builder()
                    .shortCode(activity.getShortCode())
                    .accountNumber(activity.getAccountNumber())
                    .action(activity.getAction())
                    .performedBy(activity.getPerformedBy())
                    .remarks(activity.getRemarks())
                    .actionDate(activity.getActionDate())
                    .build())
            .collect(Collectors.toList());

}

    public List<WeeklySummaryDto> getWeeklySummary() {

    List<WeeklySummaryDto> summary = new ArrayList<>();

    LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);

    for (int i = 0; i < 7; i++) {

        LocalDate currentDay = monday.plusDays(i);

        LocalDateTime start = currentDay.atStartOfDay();
        LocalDateTime end = currentDay.plusDays(1).atStartOfDay();

        long count = shortCodeRepo.countInitiatedBetween(start, end);

        summary.add(
                WeeklySummaryDto.builder()
                        .day(currentDay.getDayOfWeek().name())
                        .requests(count)
                        .build()
        );
    }

    return summary;
}

}