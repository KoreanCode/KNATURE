package com.knature.bo.controller;

import com.knature.bo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<?> dashboard() {
        return ResponseEntity.ok(Map.of(
                "orderCounts", dashboardService.getOrderStatusCounts(),
                "csCounts", dashboardService.getCsStatusCounts(),
                "todayProcessed", dashboardService.getTodayProcessedCounts(),
                "todaySales", dashboardService.getTodaySales(),
                "todayOrders", dashboardService.getTodayOrderCount(),
                "weeklySales", dashboardService.getWeeklySales(),
                "totalMembers", dashboardService.getTotalMembers(),
                "newMembers", dashboardService.getTodayNewMembers(),
                "totalProducts", dashboardService.getTotalProducts()
        ));
    }
}
