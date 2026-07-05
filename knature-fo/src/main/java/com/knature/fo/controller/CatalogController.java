package com.knature.fo.controller;

import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductStatus;
import com.knature.common.repository.ProductCategoryRepository;
import com.knature.common.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/** FO 카탈로그 — 진열된 상품만 공개 조회 (숨김 제외, 품절은 SOLD 표시용으로 포함) */
@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @GetMapping("/api/categories")
    public ResponseEntity<?> categories() {
        return ResponseEntity.ok(categoryRepository.findByParentIsNullOrderBySortOrder());
    }

    @GetMapping("/api/products")
    public ResponseEntity<?> products(@RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "newest") String sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("displayed")));
            predicates.add(cb.notEqual(root.get("status"), ProductStatus.HIDDEN));
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }
            // 정렬 (판매가는 할인가 우선 — COALESCE(salePrice, price))
            var effectivePrice = cb.coalesce(root.get("salePrice"), root.get("price"));
            switch (sort) {
                case "priceAsc" -> query.orderBy(cb.asc(effectivePrice));
                case "priceDesc" -> query.orderBy(cb.desc(effectivePrice));
                case "name" -> query.orderBy(cb.asc(root.get("name")));
                default -> query.orderBy(cb.desc(root.get("id"))); // newest
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(page, Math.min(size, 60));
        return ResponseEntity.ok(productRepository.findAll(spec, pageable));
    }

    @GetMapping("/api/products/{id}")
    public ResponseEntity<?> product(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> Boolean.TRUE.equals(p.getDisplayed()) && p.getStatus() != ProductStatus.HIDDEN)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        return ResponseEntity.ok(product);
    }
}
