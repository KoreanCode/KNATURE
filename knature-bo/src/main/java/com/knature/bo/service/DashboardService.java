package com.knature.bo.service;

import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.scm.PurchaseOrder.PoStatus;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.OrderRepository;
import com.knature.common.repository.OrderStatusHistoryRepository;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    /**
     * 기간별 매출 시리즈 — daily: 최근 14일 / weekly: 최근 8주 / monthly: 최근 6개월
     * 반환: {series: {라벨: 매출}, total, average}
     */
    public Map<String, Object> getSalesSeries(String period) {
        Map<String, Long> series = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        switch (period) {
            case "weekly" -> {
                for (int i = 7; i >= 0; i--) {
                    LocalDate weekStart = today.minusWeeks(i).with(java.time.DayOfWeek.MONDAY);
                    LocalDateTime start = weekStart.atStartOfDay();
                    LocalDateTime end = weekStart.plusWeeks(1).atStartOfDay();
                    series.put(weekStart.getMonthValue() + "/" + weekStart.getDayOfMonth() + "주",
                            orderRepository.sumPaymentAmountBetween(start, end));
                }
            }
            case "monthly" -> {
                for (int i = 5; i >= 0; i--) {
                    LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
                    LocalDateTime start = monthStart.atStartOfDay();
                    LocalDateTime end = monthStart.plusMonths(1).atStartOfDay();
                    series.put(monthStart.getYear() % 100 + "." + monthStart.getMonthValue() + "월",
                            orderRepository.sumPaymentAmountBetween(start, end));
                }
            }
            default -> { // daily
                for (int i = 13; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    LocalDateTime start = date.atStartOfDay();
                    series.put(date.getMonthValue() + "/" + date.getDayOfMonth(),
                            orderRepository.sumPaymentAmountBetween(start, start.plusDays(1)));
                }
            }
        }
        long total = series.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("series", series);
        result.put("total", total);
        result.put("average", series.isEmpty() ? 0 : total / series.size());
        return result;
    }

    /** 발주 미처리 현황 — 승인대기 건수 + 납기 임박(D-3)/초과 건수 (2차 SCM) */
    public Map<String, Long> getPoAlerts() {
        LocalDate today = LocalDate.now();
        var open = purchaseOrderRepository.findAll().stream().filter(po -> po.isOpen()).toList();
        Map<String, Long> alerts = new LinkedHashMap<>();
        alerts.put("승인대기", purchaseOrderRepository.countByStatus(PoStatus.REQUESTED));
        alerts.put("납기임박", open.stream().filter(po -> po.getDueDate() != null
                && !po.getDueDate().isBefore(today)
                && !po.getDueDate().isAfter(today.plusDays(3))).count());
        alerts.put("납기초과", open.stream().filter(po -> po.getDueDate() != null
                && po.getDueDate().isBefore(today)).count());
        return alerts;
    }

    /** 재고 부족 상품 (안전재고 이하) — 자동발주 트리거 대상 */
    public List<Map<String, Object>> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getSafetyStock() != null && p.getSafetyStock() > 0)
                .filter(p -> (p.getStockQuantity() == null ? 0 : p.getStockQuantity()) <= p.getSafetyStock())
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(), "name", p.getName(),
                        "stock", p.getStockQuantity() == null ? 0 : p.getStockQuantity(),
                        "safetyStock", p.getSafetyStock()))
                .toList();
    }

    /** 오늘 처리한 일 — 상태변경 이력 기반 (입금확인/배송처리/취소/환불 완료 건수) */
    public Map<String, Long> getTodayProcessedCounts() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("입금확인", statusHistoryRepository.countByToStatusBetween(OrderStatus.PREPARING, start, end));
        counts.put("배송처리", statusHistoryRepository.countByToStatusBetween(OrderStatus.SHIPPING, start, end));
        counts.put("취소완료", statusHistoryRepository.countByToStatusBetween(OrderStatus.CANCELLED, start, end));
        counts.put("환불완료", statusHistoryRepository.countByToStatusBetween(OrderStatus.REFUND_COMPLETED, start, end));
        return counts;
    }

    public Map<String, Long> getOrderStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("입금전", orderRepository.countByStatus(OrderStatus.PENDING_PAYMENT));
        counts.put("배송준비중", orderRepository.countByStatus(OrderStatus.PREPARING));
        counts.put("배송중", orderRepository.countByStatus(OrderStatus.SHIPPING));
        return counts;
    }

    public Map<String, Long> getCsStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("취소요청", orderRepository.countByStatus(OrderStatus.CANCEL_REQUESTED));
        counts.put("교환요청", orderRepository.countByStatus(OrderStatus.EXCHANGE_REQUESTED));
        counts.put("반품요청", orderRepository.countByStatus(OrderStatus.RETURN_REQUESTED));
        return counts;
    }

    public long getTodaySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return orderRepository.sumPaymentAmountBetween(start, end);
    }

    public long getTodayOrderCount() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return orderRepository.countOrdersBetween(start, end);
    }

    public Map<String, Long> getWeeklySales() {
        Map<String, Long> sales = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            sales.put(date.getMonthValue() + "/" + date.getDayOfMonth(),
                    orderRepository.sumPaymentAmountBetween(start, end));
        }
        return sales;
    }

    public long getTotalMembers() {
        return memberRepository.countActive();
    }

    public long getTodayNewMembers() {
        return memberRepository.countTodayJoined();
    }

    public long getTotalProducts() {
        return productRepository.count();
    }
}
