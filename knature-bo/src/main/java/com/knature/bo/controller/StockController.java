package com.knature.bo.controller;

import com.knature.bo.service.StockService;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.StockLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ProductRepository productRepository;
    private final StockLotRepository stockLotRepository;

    /** 안전재고 설정 — 이하 시 부족 알림 + 자동 발주 트리거 (2차) */
    @PatchMapping("/{productId}/safety")
    @Transactional
    public ResponseEntity<?> updateSafetyStock(@PathVariable Long productId, @RequestBody Map<String, String> body) {
        int safety = Integer.parseInt(body.get("safetyStock"));
        if (safety < 0) throw new IllegalArgumentException("안전재고는 0 이상이어야 합니다.");
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        product.setSafetyStock(safety);
        return ResponseEntity.ok(Map.of("message", "안전재고가 " + safety + "개로 설정되었습니다."));
    }

    /** LOT/유통기한 조회 (유통기한 임박순) */
    @GetMapping("/lots")
    public ResponseEntity<?> lots(@RequestParam(required = false) Long productId) {
        return ResponseEntity.ok(productId != null
                ? stockLotRepository.findByProductIdOrderByExpiryDateAsc(productId)
                : stockLotRepository.findAllByOrderByExpiryDateAsc());
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(stockService.getStocks(keyword, pageable));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<?> adjust(@PathVariable Long productId,
                                    @RequestBody Map<String, String> body,
                                    Authentication authentication) {
        int quantity = Integer.parseInt(body.get("quantity"));
        String reason = body.get("reason");
        stockService.adjustStock(productId, quantity, reason, authentication.getName());
        return ResponseEntity.ok(Map.of("message", "재고가 조정되었습니다."));
    }

    @GetMapping("/{productId}/history")
    public ResponseEntity<?> history(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.getHistory(productId));
    }
}
