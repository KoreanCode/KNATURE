package com.knature.bo.controller;

import com.knature.bo.util.ExcelUtil;
import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * BO 매출 상세 분석 (3차) — 기간 지정 요약/일별/상품별/등급별/결제수단별 + 엑셀 리포트.
 * 집계 기준은 대시보드와 동일: 취소완료/환불완료 제외, 결제금액(paymentAmount) 합산.
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private static final List<OrderStatus> EXCLUDED = List.of(OrderStatus.CANCELLED, OrderStatus.REFUND_COMPLETED);
    private static final String[] GRADE_LABELS = {"NEW:뉴", "RUBY:루비", "SILVER:실버", "GOLD:골드", "DIAMOND:다이아몬드", "PLATINUM:플래티넘"};

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<?> analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Range range = normalize(from, to);
        List<Order> orders = findOrders(range);

        long total = orders.stream().mapToLong(o -> nz(o.getPaymentAmount())).sum();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("from", range.from.toString());
        summary.put("to", range.to.toString());
        summary.put("totalSales", total);
        summary.put("orderCount", orders.size());
        summary.put("avgOrderAmount", orders.isEmpty() ? 0 : total / orders.size());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("summary", summary);
        result.put("daily", aggregateDaily(orders, range));
        result.put("byProduct", aggregateByProduct(orders));
        result.put("byGrade", aggregateByGrade(orders));
        result.put("byPayment", aggregateByPayment(orders));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) throws IOException {
        Range range = normalize(from, to);
        List<Order> orders = findOrders(range);
        long total = orders.stream().mapToLong(o -> nz(o.getPaymentAmount())).sum();

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{"요약", "총매출", orders.size(), total});
        rows.add(new Object[]{"요약", "평균 주문금액", null, orders.isEmpty() ? 0 : total / orders.size()});
        for (Map<String, Object> d : aggregateDaily(orders, range)) {
            rows.add(new Object[]{"일별", d.get("date"), d.get("orders"), d.get("total")});
        }
        for (Map<String, Object> p : aggregateByProduct(orders)) {
            rows.add(new Object[]{"상품별", p.get("name"), p.get("quantity"), p.get("total")});
        }
        for (Map<String, Object> g : aggregateByGrade(orders)) {
            rows.add(new Object[]{"등급별", g.get("grade"), g.get("orders"), g.get("total")});
        }
        for (Map<String, Object> p : aggregateByPayment(orders)) {
            rows.add(new Object[]{"결제수단", p.get("method"), p.get("orders"), p.get("total")});
        }
        String filename = "매출분석_" + range.from + "_" + range.to + ".xlsx";
        return ExcelUtil.download(filename, "매출분석",
                new String[]{"구분", "항목", "건수/수량", "금액(원)"}, rows);
    }

    // ===== 집계 =====

    private record Range(LocalDate from, LocalDate to) {}

    private Range normalize(LocalDate from, LocalDate to) {
        LocalDate t = to != null ? to : LocalDate.now();
        LocalDate f = from != null ? from : t.minusDays(29);
        if (f.isAfter(t)) throw new IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.");
        if (f.plusYears(1).isBefore(t)) throw new IllegalArgumentException("조회 기간은 최대 1년입니다.");
        return new Range(f, t);
    }

    private List<Order> findOrders(Range range) {
        Specification<Order> spec = (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), range.from.atStartOfDay()),
                cb.lessThan(root.get("createdAt"), range.to.plusDays(1).atStartOfDay()),
                cb.not(root.get("status").in(EXCLUDED)));
        return orderRepository.findAll(spec);
    }

    private List<Map<String, Object>> aggregateDaily(List<Order> orders, Range range) {
        Map<LocalDate, long[]> map = new TreeMap<>(); // [건수, 금액]
        for (LocalDate d = range.from; !d.isAfter(range.to); d = d.plusDays(1)) {
            map.put(d, new long[]{0, 0});
        }
        for (Order o : orders) {
            long[] acc = map.get(o.getCreatedAt().toLocalDate());
            if (acc != null) { acc[0]++; acc[1] += nz(o.getPaymentAmount()); }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        map.forEach((date, acc) -> result.add(new LinkedHashMap<>(
                Map.of("date", date.toString(), "orders", acc[0], "total", acc[1]))));
        return result;
    }

    private List<Map<String, Object>> aggregateByProduct(List<Order> orders) {
        Map<String, long[]> map = new LinkedHashMap<>(); // [수량, 금액]
        orders.forEach(o -> o.getItems().forEach(item -> {
            String name = item.getProductName() + (item.getOptionName() != null ? " (" + item.getOptionName() + ")" : "");
            long[] acc = map.computeIfAbsent(name, k -> new long[]{0, 0});
            acc[0] += item.getQuantity();
            acc[1] += nz(item.getPrice()) * item.getQuantity();
        }));
        return map.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue()[1], a.getValue()[1]))
                .map(e -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("name", e.getKey());
                    m.put("quantity", e.getValue()[0]);
                    m.put("total", e.getValue()[1]);
                    return m;
                }).toList();
    }

    private List<Map<String, Object>> aggregateByGrade(List<Order> orders) {
        Map<String, long[]> map = new LinkedHashMap<>();
        for (String g : GRADE_LABELS) map.put(g.split(":")[1], new long[]{0, 0});
        map.put("비회원", new long[]{0, 0});
        for (Order o : orders) {
            String grade = "비회원";
            if (o.getMember() != null) {
                String key = o.getMember().getGrade().name();
                grade = Arrays.stream(GRADE_LABELS).filter(g -> g.startsWith(key + ":"))
                        .map(g -> g.split(":")[1]).findFirst().orElse(key);
            }
            long[] acc = map.computeIfAbsent(grade, k -> new long[]{0, 0});
            acc[0]++;
            acc[1] += nz(o.getPaymentAmount());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        map.forEach((grade, acc) -> {
            if (acc[0] == 0) return; // 실적 없는 등급 생략
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("grade", grade);
            m.put("orders", acc[0]);
            m.put("total", acc[1]);
            result.add(m);
        });
        return result;
    }

    private List<Map<String, Object>> aggregateByPayment(List<Order> orders) {
        Map<String, long[]> map = new LinkedHashMap<>();
        for (Order o : orders) {
            long[] acc = map.computeIfAbsent(o.getPaymentMethod().getLabel(), k -> new long[]{0, 0});
            acc[0]++;
            acc[1] += nz(o.getPaymentAmount());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        map.forEach((method, acc) -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("method", method);
            m.put("orders", acc[0]);
            m.put("total", acc[1]);
            result.add(m);
        });
        return result;
    }

    private long nz(Long v) { return v == null ? 0 : v; }
}
