package com.knature.bo.service;

import com.knature.common.domain.order.OrderStatus;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.OrderRepository;
import com.knature.common.repository.OrderStatusHistoryRepository;
import com.knature.common.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;

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
