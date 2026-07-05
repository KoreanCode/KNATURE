package com.knature.bo.controller;

import com.knature.bo.service.OrderService;
import com.knature.bo.util.CsvUtil;
import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) OrderStatus status,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrders(keyword, status, from, to, pageable));
    }

    /** 주문 목록 엑셀 다운로드 (조회 필터 동일 적용, Excel 호환 CSV) */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) OrderStatus status,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"주문번호", "주문자", "연락처", "결제금액", "결제수단", "상태", "택배사", "송장번호", "주문일시"});
        for (Order o : orderService.getOrdersForExcel(keyword, status, from, to)) {
            rows.add(new String[]{
                    o.getOrderNumber(),
                    o.getOrdererName(),
                    o.getOrdererPhone(),
                    String.valueOf(o.getPaymentAmount()),
                    o.getPaymentMethod().getLabel(),
                    o.getStatus().getLabel(),
                    o.getCourierCompany(),
                    o.getTrackingNumber(),
                    o.getCreatedAt() != null ? o.getCreatedAt().toString().replace('T', ' ') : ""
            });
        }
        return CsvUtil.download("주문목록.csv", rows);
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

    /** 송장 입력 (택배사/송장번호) → 배송중 전환 — FO 배송추적 연동 */
    @PatchMapping("/{id}/shipping")
    public ResponseEntity<?> updateShipping(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.updateShipping(id, body.get("courierCompany"), body.get("trackingNumber"));
        return ResponseEntity.ok(Map.of("message", "송장이 등록되어 배송중으로 변경되었습니다."));
    }
}
