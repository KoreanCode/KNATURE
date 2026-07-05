package com.knature.bo.controller;

import com.knature.bo.service.OrderService;
import com.knature.bo.util.CsvUtil;
import com.knature.bo.util.ExcelUtil;
import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
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

    /** 주문 목록 엑셀(.xlsx) 다운로드 — 셀 타입 지정(송장번호=텍스트, 금액=숫자, 일시=날짜) */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) OrderStatus status,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws IOException {
        String[] headers = {"주문번호", "주문자", "연락처", "결제금액", "결제수단", "상태", "택배사", "송장번호", "주문일시"};
        List<Object[]> rows = new ArrayList<>();
        for (Order o : orderService.getOrdersForExcel(keyword, status, from, to)) {
            rows.add(new Object[]{
                    o.getOrderNumber(),
                    o.getOrdererName(),
                    o.getOrdererPhone(),
                    o.getPaymentAmount(),          // 숫자 셀 (#,##0)
                    o.getPaymentMethod().getLabel(),
                    o.getStatus().getLabel(),
                    o.getCourierCompany(),
                    o.getTrackingNumber(),          // 텍스트 셀 (지수표기 방지)
                    o.getCreatedAt()                // 날짜 셀 (yyyy-mm-dd hh:mm)
            });
        }
        return ExcelUtil.download("주문목록.xlsx", "주문목록", headers, rows);
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

    /** 품목별 조회 탭 — 주문 조건으로 OrderItem 목록 */
    @GetMapping("/items")
    public ResponseEntity<?> items(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) OrderStatus status,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrderItems(keyword, status, from, to, pageable));
    }

    /** 송장 일괄 등록용 CSV 템플릿 다운로드 */
    @GetMapping("/shipping-template")
    public ResponseEntity<byte[]> shippingTemplate() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"주문번호", "택배사", "송장번호"});
        rows.add(new String[]{"ORD-20260705-001", "CJ대한통운", "123456789012"});
        return CsvUtil.download("송장일괄등록_템플릿.csv", rows);
    }

    /** 송장 일괄 등록 — CSV 업로드 (주문번호,택배사,송장번호 / Excel 저장 CSV 호환: UTF-8·MS949) */
    @PostMapping("/shipping-bulk")
    public ResponseEntity<?> shippingBulk(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어 있습니다.");
        }
        String content = decodeCsv(file.getBytes());
        List<String[]> rows = new ArrayList<>();
        String[] lines = content.split("\r?\n");
        for (int i = 1; i < lines.length; i++) { // 1행(헤더) 제외
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            rows.add(line.split(","));
        }
        return ResponseEntity.ok(orderService.bulkShipping(rows));
    }

    /** CSV 인코딩 감지: UTF-8(BOM 포함) 우선, 실패 시 MS949(엑셀 한글 기본) */
    private static String decodeCsv(byte[] bytes) {
        int offset = (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) ? 3 : 0;
        try {
            CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
            return decoder.decode(ByteBuffer.wrap(bytes, offset, bytes.length - offset)).toString();
        } catch (Exception e) {
            return new String(bytes, offset, bytes.length - offset, Charset.forName("MS949"));
        }
    }
}
