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
        long useDeposit = body.get("useDeposit") != null ? ((Number) body.get("useDeposit")).longValue() : 0;
        Long memberCouponId = body.get("memberCouponId") != null ? ((Number) body.get("memberCouponId")).longValue() : null;

        // 비회원 주문 (2차): 비로그인 + guest 정보 포함
        FoOrderService.GuestInfo guest = null;
        if (auth == null) {
            guest = new FoOrderService.GuestInfo(
                    (String) body.get("guestName"), (String) body.get("guestEmail"),
                    (String) body.get("guestPhone"), (String) body.get("guestPassword"));
        }

        Order order = orderService.createOrder(
                auth != null ? auth.getName() : null,
                items,
                PaymentMethod.valueOf((String) body.get("paymentMethod")),
                (String) body.get("receiverName"),
                (String) body.get("receiverPhone"),
                (String) body.get("zipcode"),
                (String) body.get("address"),
                (String) body.get("addressDetail"),
                (String) body.get("deliveryMemo"),
                useMileage,
                memberCouponId,
                useDeposit,
                guest
        );
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNumber", order.getOrderNumber());
        result.put("paymentAmount", order.getPaymentAmount());
        result.put("usedMileage", order.getUsedMileage());
        result.put("usedDeposit", order.getUsedDeposit());
        result.put("couponDiscount", order.getCouponDiscount());
        result.put("paymentMethod", order.getPaymentMethod().name());
        result.put("status", order.getStatus().name());
        result.put("guest", order.isGuest());
        return ResponseEntity.ok(result);
    }

    /** 비회원 주문 조회 — 주문번호 + 주문 비밀번호 (2차) */
    @PostMapping("/guest/lookup")
    public ResponseEntity<?> guestLookup(@RequestBody Map<String, String> body) {
        Order order = orderService.getGuestOrder(body.get("orderNumber"), body.get("password"));
        return ResponseEntity.ok(order);
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
