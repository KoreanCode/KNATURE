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
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("orderCounts", dashboardService.getOrderStatusCounts());
        result.put("csCounts", dashboardService.getCsStatusCounts());
        result.put("todayProcessed", dashboardService.getTodayProcessedCounts());
        result.put("poAlerts", dashboardService.getPoAlerts());
        result.put("lowStock", dashboardService.getLowStockProducts());
        result.put("todaySales", dashboardService.getTodaySales());
        result.put("todayOrders", dashboardService.getTodayOrderCount());
        result.put("weeklySales", dashboardService.getWeeklySales());
        result.put("totalMembers", dashboardService.getTotalMembers());
        result.put("newMembers", dashboardService.getTodayNewMembers());
        result.put("totalProducts", dashboardService.getTotalProducts());
        return ResponseEntity.ok(result);
    }

    /** 기간별 매출 — period: daily(14일) | weekly(8주) | monthly(6개월) */
    @GetMapping("/sales")
    public ResponseEntity<?> sales(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(dashboardService.getSalesSeries(period));
    }
}
