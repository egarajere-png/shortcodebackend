package com.abcbank.shortcode.shortcode.controllers;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.abcbank.shortcode.shortcode.dto.DashboardAnalyticsDto;
import com.abcbank.shortcode.shortcode.dto.RecentActivityDto;
import com.abcbank.shortcode.shortcode.dto.WeeklySummaryDto;
import com.abcbank.shortcode.shortcode.services.DashboardService;
import jakarta.annotation.security.RolesAllowed;


/**
 * Exposes dashboard analytics endpoints.
 *
 * Provides summary statistics, recent activity,
 * and weekly request trends.
 */

@RestController

@RequestMapping("/shortcodes/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/analytics")
    @RolesAllowed({"apicaller"})
    public DashboardAnalyticsDto analytics() {
        return dashboardService.getAnalytics();
    }


    /**
 * Returns recent audit activities.
 */

    @GetMapping("/recent-activity")
    @RolesAllowed({"apicaller"})
    public List<RecentActivityDto> recentActivity() {
        return dashboardService.getRecentActivity();
    }


    /**
 * Returns weekly shortcode request statistics.
 */
    @GetMapping("/weekly-summary")
    @RolesAllowed({"apicaller"})
    public List<WeeklySummaryDto> weeklySummary() {
        return dashboardService.getWeeklySummary();
    }
}