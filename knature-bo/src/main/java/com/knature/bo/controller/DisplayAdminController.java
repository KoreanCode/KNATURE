package com.knature.bo.controller;

import com.knature.common.domain.display.ProductDisplay;
import com.knature.common.domain.display.ProductDisplay.Section;
import com.knature.common.domain.product.Product;
import com.knature.common.repository.ProductDisplayRepository;
import com.knature.common.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** BO 진열 관리 — 메인 MD CHOICE / MONTH BEST 섹션 (2차) */
@RestController
@RequestMapping("/api/displays")
@RequiredArgsConstructor
public class DisplayAdminController {

    private final ProductDisplayRepository displayRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(displayRepository.findAllByOrderBySectionAscSortOrderAscIdAsc());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, String> body) {
        Section section = Section.valueOf(required(body.get("section"), "섹션"));
        Long productId = Long.parseLong(required(body.get("productId"), "상품"));
        if (displayRepository.existsBySectionAndProductId(section, productId)) {
            throw new IllegalArgumentException("이미 해당 섹션에 진열된 상품입니다.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        int sortOrder = body.get("sortOrder") != null && !body.get("sortOrder").isBlank()
                ? Integer.parseInt(body.get("sortOrder")) : 0;
        return ResponseEntity.ok(displayRepository.save(ProductDisplay.builder()
                .section(section).product(product).sortOrder(sortOrder).build()));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ProductDisplay display = displayRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("진열 항목을 찾을 수 없습니다."));
        if (body.get("sortOrder") != null) display.setSortOrder(Integer.parseInt(body.get("sortOrder")));
        return ResponseEntity.ok(display);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(@PathVariable Long id) {
        displayRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "진열에서 제외되었습니다."));
    }

    private String required(String v, String label) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + "을(를) 입력해주세요.");
        return v;
    }
}
