package com.knature.bo.controller;

import com.knature.bo.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

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
