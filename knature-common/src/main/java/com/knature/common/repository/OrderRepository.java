package com.knature.common.repository;

import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.order.PaymentMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Optional<Order> findByOrderNumber(String orderNumber);

    long countByOrderNumberStartingWith(String prefix);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByOrderNumberContainingOrOrdererNameContaining(String orderNumber, String ordererName, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.paymentAmount), 0) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to AND o.status NOT IN ('CANCELLED', 'REFUND_COMPLETED')")
    long sumPaymentAmountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to")
    long countOrdersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);

    List<Order> findByStatusAndPaymentMethodAndCreatedAtBefore(OrderStatus status, PaymentMethod paymentMethod, LocalDateTime dateTime);

    List<Order> findByMemberIdOrderByIdDesc(Long memberId);

    List<Order> findByMemberIdAndCreatedAtAfterOrderByIdDesc(Long memberId, LocalDateTime after);
}
