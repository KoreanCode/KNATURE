package com.knature.common.repository;

import com.knature.common.domain.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long>, JpaSpecificationExecutor<OrderItem> {
    long countByProductId(Long productId);

    /** 배송완료 주문에 해당 상품이 있는지 (후기 작성 자격 검증) */
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.product.id = :productId AND oi.order.member.id = :memberId AND oi.order.status = com.knature.common.domain.order.OrderStatus.DELIVERED")
    long countDeliveredPurchase(@Param("memberId") Long memberId, @Param("productId") Long productId);
}
