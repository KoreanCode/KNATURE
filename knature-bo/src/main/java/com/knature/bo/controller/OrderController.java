package com.knature.bo.controller;

import com.knature.bo.service.OrderService;
import com.knature.common.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) OrderStatus status,
                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(keyword, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateStatus(id, OrderStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(Map.of("message", "주문 상태가 변경되었습니다."));
    }

    @PatchMapping("/{id}/memo")
    public ResponseEntity<?> updateMemo(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateMemo(id, body.get("memo"));
        return ResponseEntity.ok(Map.of("message", "메모가 저장되었습니다."));
    }
}
