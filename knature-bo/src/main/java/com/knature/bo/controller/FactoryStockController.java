package com.knature.bo.controller;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.scm.Factory;
import com.knature.common.domain.scm.FactoryStock;
import com.knature.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 공장별 재고 — 최고관리자: 전체 조회 / 공장관리자: 자기 공장 조회·수정 */
@RestController
@RequestMapping("/api/factory-stocks")
@RequiredArgsConstructor
public class FactoryStockController {

    private final FactoryStockRepository factoryStockRepository;
    private final FactoryRepository factoryRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;

    private Admin admin(Authentication auth) {
        return adminRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) Long factoryId, Authentication auth) {
        Admin me = admin(auth);
        if (me.getRole().name().equals("FACTORY_ADMIN")) {
            if (me.getFactory() == null) throw new IllegalArgumentException("소속 공장이 지정되지 않았습니다.");
            factoryId = me.getFactory().getId();
        }
        return ResponseEntity.ok(factoryId != null
                ? factoryStockRepository.findByFactoryId(factoryId)
                : factoryStockRepository.findAll());
    }

    /** 공장 재고 입력/수정 — 공장관리자는 자기 공장만 */
    @PutMapping
    @Transactional
    public ResponseEntity<?> upsert(@RequestBody Map<String, String> body, Authentication auth) {
        Admin me = admin(auth);
        Long factoryId = Long.parseLong(body.get("factoryId"));
        if (me.getRole().name().equals("FACTORY_ADMIN")) {
            if (me.getFactory() == null || !me.getFactory().getId().equals(factoryId)) {
                throw new IllegalArgumentException("소속 공장의 재고만 수정할 수 있습니다.");
            }
        }
        Long productId = Long.parseLong(body.get("productId"));
        int quantity = Integer.parseInt(body.get("quantity"));
        if (quantity < 0) throw new IllegalArgumentException("재고는 0 이상이어야 합니다.");

        FactoryStock stock = factoryStockRepository.findByFactoryIdAndProductId(factoryId, productId)
                .orElseGet(() -> {
                    Factory factory = factoryRepository.findById(factoryId)
                            .orElseThrow(() -> new IllegalArgumentException("공장을 찾을 수 없습니다."));
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
                    return FactoryStock.builder().factory(factory).product(product).build();
                });
        stock.setQuantity(quantity);
        factoryStockRepository.save(stock);
        return ResponseEntity.ok(Map.of("message", "공장 재고가 저장되었습니다."));
    }
}
