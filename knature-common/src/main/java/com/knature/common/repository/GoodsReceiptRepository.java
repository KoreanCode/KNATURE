package com.knature.common.repository;

import com.knature.common.domain.scm.GoodsReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    List<GoodsReceipt> findByPurchaseOrderIdOrderByIdDesc(Long purchaseOrderId);
}
