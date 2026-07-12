package com.knature.common.repository;

import com.knature.common.domain.scm.PurchaseOrder;
import com.knature.common.domain.scm.PurchaseOrder.PoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
    long countByPoNumberStartingWith(String prefix);

    long countByStatus(PoStatus status);

    /** 미완료(진행 중) 발주 존재 여부 — 자동발주 중복 방지 */
    List<PurchaseOrder> findByProductIdAndStatusIn(Long productId, List<PoStatus> statuses);
}
