package com.knature.bo.controller;

import com.knature.bo.service.ProductService;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductCategory;
import com.knature.common.domain.product.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) ProductStatus status,
                                  @RequestParam(required = false) Long categoryId,
                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(productService.getProducts(keyword, status, categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Product product) {
        product.setId(null); // mass assignment 방지 — 생성 시 id 지정 불가
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.saveProduct(product));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        productService.updateStatus(id, ProductStatus.valueOf(body.get("status")));
        return ResponseEntity.ok(Map.of("message", "상태가 변경되었습니다."));
    }

    /** 선택 상품 상태 일괄 변경 */
    @PatchMapping("/bulk-status")
    public ResponseEntity<?> updateStatusBulk(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Number> rawIds = (List<Number>) body.get("ids");
        List<Long> ids = rawIds.stream().map(Number::longValue).toList();
        ProductStatus status = ProductStatus.valueOf((String) body.get("status"));
        int count = productService.updateStatusBulk(ids, status);
        return ResponseEntity.ok(Map.of("message", count + "개 상품의 상태가 변경되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "상품이 삭제되었습니다."));
    }

    // ===== 상품 분류(카테고리) CRUD — FO 카테고리 GNB 연동 =====

    @GetMapping("/categories")
    public ResponseEntity<List<ProductCategory>> categories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> body) {
        Integer sortOrder = body.get("sortOrder") != null ? Integer.valueOf(body.get("sortOrder")) : null;
        return ResponseEntity.ok(productService.createCategory(body.get("name"), body.get("slug"), sortOrder));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Integer sortOrder = body.get("sortOrder") != null ? Integer.valueOf(body.get("sortOrder")) : null;
        return ResponseEntity.ok(productService.updateCategory(id, body.get("name"), body.get("slug"), sortOrder));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "분류가 삭제되었습니다."));
    }
}
