package com.knature.bo.service;

import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.order.PaymentMethod;
import com.knature.common.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<Order> getOrders(String keyword, OrderStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return orderRepository.findAll(buildSpec(keyword, status, from, to), pageable);
    }

    public List<Order> getOrdersForExcel(String keyword, OrderStatus status, LocalDate from, LocalDate to) {
        return orderRepository.findAll(buildSpec(keyword, status, from, to));
    }

    private Specification<Order> buildSpec(String keyword, OrderStatus status, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("orderNumber"), like),
                        cb.like(root.get("ordererName"), like)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order order = getOrder(id);
        order.setStatus(status);
    }

    @Transactional
    public void updateMemo(Long id, String memo) {
        Order order = getOrder(id);
        order.setAdminMemo(memo);
    }

    /** 송장 입력 → 배송중 전환 */
    @Transactional
    public void updateShipping(Long id, String courierCompany, String trackingNumber) {
        if (courierCompany == null || courierCompany.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("택배사와 송장번호를 모두 입력해주세요.");
        }
        Order order = getOrder(id);
        order.setCourierCompany(courierCompany);
        order.setTrackingNumber(trackingNumber);
        order.setStatus(OrderStatus.SHIPPING);
    }

    /** 무통장입금 미입금 7일 경과 자동 취소 (스케줄러에서 호출) */
    @Transactional
    public int cancelExpiredBankTransferOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Order> expired = orderRepository.findByStatusAndPaymentMethodAndCreatedAtBefore(
                OrderStatus.PENDING_PAYMENT, PaymentMethod.BANK_TRANSFER, threshold);
        for (Order order : expired) {
            order.setStatus(OrderStatus.CANCELLED);
            String memo = order.getAdminMemo() == null ? "" : order.getAdminMemo() + "\n";
            order.setAdminMemo(memo + "[시스템] 무통장입금 7일 미입금 자동취소");
        }
        if (!expired.isEmpty()) {
            log.info("무통장 미입금 자동취소 처리: {}건", expired.size());
        }
        return expired.size();
    }
}
