package com.knature.bo.service;

import com.knature.common.domain.product.Product;
import com.knature.common.domain.stock.StockHistory;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;

    public Page<Product> getStocks(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByNameContaining(keyword, pageable);
        }
        return productRepository.findAll(pageable);
    }

    /**
     * 재고 수동 조정 (사유 필수, 조정 이력 기록)
     * 품절 자동 처리: 재고 0 → SOLD_OUT / 재고 확보 시 SOLD_OUT → ON_SALE (FO SOLD 배지 연동)
     */
    @Transactional
    public StockHistory adjustStock(Long productId, int quantity, String reason, String adjustedBy) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 수량은 0 이상이어야 합니다.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("조정 사유를 입력해주세요.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        int before = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        product.setStockQuantity(quantity);
        product.applyStockStatusRule(); // 품절 자동 처리/해제 (엔티티 공통 규칙)

        return stockHistoryRepository.save(StockHistory.builder()
                .product(product)
                .beforeQuantity(before)
                .afterQuantity(quantity)
                .reason(reason)
                .adjustedBy(adjustedBy)
                .build());
    }

    public List<StockHistory> getHistory(Long productId) {
        return stockHistoryRepository.findByProductIdOrderByIdDesc(productId);
    }
}
