package com.knature.bo.service;

import com.knature.common.domain.order.OrderStatus;
import com.knature.common.repository.MemberRepository;
import com.knature.common.repository.OrderRepository;
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
