package com.knature.bo.controller;

import com.knature.bo.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orderCounts", dashboardService.getOrderStatusCounts());
        model.addAttribute("csCounts", dashboardService.getCsStatusCounts());
        model.addAttribute("todaySales", dashboardService.getTodaySales());
        model.addAttribute("todayOrders", dashboardService.getTodayOrderCount());
        model.addAttribute("weeklySales", dashboardService.getWeeklySales());
        model.addAttribute("totalMembers", dashboardService.getTotalMembers());
        model.addAttribute("newMembers", dashboardService.getTodayNewMembers());
        model.addAttribute("totalProducts", dashboardService.getTotalProducts());
        return "dashboard/index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
