package com.knature.common.repository;

import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByOrderIdOrderByIdDesc(Long orderId);

    @Query("SELECT COUNT(h) FROM OrderStatusHistory h WHERE h.toStatus = :toStatus AND h.createdAt >= :from AND h.createdAt < :to")
    long countByToStatusBetween(@Param("toStatus") OrderStatus toStatus,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);
}
