package com.knature.fo.controller;

import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.PaymentMethod;
import com.knature.fo.service.FoOrderService;
import com.knature.fo.service.FoOrderService.OrderItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class FoOrderController {

    private final FoOrderService orderService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> body, Authentication auth) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) body.get("items");
        if (rawItems == null) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }
        List<OrderItemRequest> items = rawItems.stream().map(m -> new OrderItemRequest(
                ((Number) m.get("productId")).longValue(),
                m.get("optionId") != null ? ((Number) m.get("optionId")).longValue() : null,
                ((Number) m.get("quantity")).intValue()
        )).toList();

        long useMileage = body.get("useMileage") != null ? ((Number) body.get("useMileage")).longValue() : 0;
        Long memberCouponId = body.get("memberCouponId") != null ? ((Number) body.get("memberCouponId")).longValue() : null;

        Order order = orderService.createOrder(
                auth.getName(),
                items,
                PaymentMethod.valueOf((String) body.get("paymentMethod")),
                (String) body.get("receiverName"),
                (String) body.get("receiverPhone"),
                (String) body.get("zipcode"),
                (String) body.get("address"),
                (String) body.get("addressDetail"),
                (String) body.get("deliveryMemo"),
                useMileage,
                memberCouponId
        );
        return ResponseEntity.ok(Map.of(
                "orderId", order.getId(),
                "orderNumber", order.getOrderNumber(),
                "paymentAmount", order.getPaymentAmount(),
                "usedMileage", order.getUsedMileage(),
                "couponDiscount", order.getCouponDiscount(),
                "paymentMethod", order.getPaymentMethod().name(),
                "status", order.getStatus().name()
        ));
    }

    @GetMapping
    public ResponseEntity<?> myOrders(@RequestParam(required = false) Integer months, Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrders(auth.getName(), months));
    }

    @GetMapping("/summary")
    public ResponseEntity<?> summary(Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrderSummary(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.getMyOrder(auth.getName(), id));
    }

    /** 취소/교환/반품 신청 — type: cancel | exchange | return */
    @PostMapping("/{id}/cs")
    public ResponseEntity<?> requestCs(@PathVariable Long id, @RequestBody Map<String, String> body,
                                       Authentication auth) {
        orderService.requestCs(auth.getName(), id, body.get("type"), body.get("reason"));
        return ResponseEntity.ok(Map.of("message", "신청이 접수되었습니다. 처리 현황은 주문 상세에서 확인하실 수 있습니다."));
    }
}
