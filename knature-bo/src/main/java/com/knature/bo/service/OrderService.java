package com.knature.bo.service;

import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;

    public Page<Order> getOrders(String keyword, OrderStatus status, Pageable pageable) {
        if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        }
        if (keyword != null && !keyword.isBlank()) {
            return orderRepository.findByOrderNumberContainingOrOrdererNameContaining(keyword, keyword, pageable);
        }
        return orderRepository.findAll(pageable);
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
}
