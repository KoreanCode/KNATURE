package com.knature.bo.controller;

import com.knature.bo.service.OrderService;
import com.knature.common.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) OrderStatus status,
                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("orders", orderService.getOrders(keyword, status, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("statuses", OrderStatus.values());
        return "order/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.getOrder(id));
        model.addAttribute("statuses", OrderStatus.values());
        return "order/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam OrderStatus status,
                               RedirectAttributes ra) {
        orderService.updateStatus(id, status);
        ra.addFlashAttribute("message", "주문 상태가 변경되었습니다.");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/memo")
    public String updateMemo(@PathVariable Long id, @RequestParam String memo,
                             RedirectAttributes ra) {
        orderService.updateMemo(id, memo);
        ra.addFlashAttribute("message", "메모가 저장되었습니다.");
        return "redirect:/admin/orders/" + id;
    }
}
