package com.knature.bo.controller;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.admin.AdminRole;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.scm.Factory;
import com.knature.common.domain.scm.FactoryProduct;
import com.knature.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** BO 공장(공급사) 관리 — CRUD + 담당자 계정 + 상품 매핑 */
@RestController
@RequestMapping("/api/factories")
@RequiredArgsConstructor
public class FactoryController {

    private final FactoryRepository factoryRepository;
    private final FactoryProductRepository factoryProductRepository;
    private final ProductRepository productRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(factoryRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body) {
        Factory factory = Factory.builder()
                .name(required(body.get("name"), "공장명"))
                .bizNumber(body.get("bizNumber"))
                .address(body.get("address"))
                .phone(body.get("phone"))
                .managerName(body.get("managerName"))
                .leadTimeDays(parseInt(body.get("leadTimeDays"), 7))
                .region(body.get("region"))
                .build();
        return ResponseEntity.ok(factoryRepository.save(factory));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Factory factory = getFactory(id);
        if (body.get("name") != null) factory.setName(body.get("name"));
        if (body.get("bizNumber") != null) factory.setBizNumber(body.get("bizNumber"));
        if (body.get("address") != null) factory.setAddress(body.get("address"));
        if (body.get("phone") != null) factory.setPhone(body.get("phone"));
        if (body.get("managerName") != null) factory.setManagerName(body.get("managerName"));
        if (body.get("leadTimeDays") != null) factory.setLeadTimeDays(parseInt(body.get("leadTimeDays"), 7));
        if (body.get("region") != null) factory.setRegion(body.get("region"));
        if (body.get("active") != null) factory.setActive("true".equals(body.get("active")));
        return ResponseEntity.ok(factory);
    }

    /** 공장 담당자 계정 생성 (FACTORY_ADMIN — 자기 공장 발주/재고만 접근) */
    @PostMapping("/{id}/manager")
    public ResponseEntity<?> createManager(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Factory factory = getFactory(id);
        String username = required(body.get("username"), "아이디");
        String password = required(body.get("password"), "비밀번호");
        if (adminRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        Admin manager = Admin.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(required(body.get("name"), "이름"))
                .role(AdminRole.FACTORY_ADMIN)
                .factory(factory)
                .build();
        adminRepository.save(manager);
        return ResponseEntity.ok(Map.of("message", factory.getName() + " 공장관리자 계정이 생성되었습니다."));
    }

    // ===== 공장-상품 매핑 =====

    @GetMapping("/{id}/products")
    public ResponseEntity<?> mappings(@PathVariable Long id) {
        return ResponseEntity.ok(factoryProductRepository.findByFactoryId(id));
    }

    @PostMapping("/{id}/products")
    public ResponseEntity<?> addMapping(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Factory factory = getFactory(id);
        Long productId = Long.parseLong(required(body.get("productId"), "상품"));
        if (factoryProductRepository.existsByFactoryIdAndProductId(id, productId)) {
            throw new IllegalArgumentException("이미 매핑된 상품입니다.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        FactoryProduct mapping = FactoryProduct.builder()
                .factory(factory).product(product)
                .unitCost(Long.parseLong(body.getOrDefault("unitCost", "0")))
                .moq(parseInt(body.get("moq"), 1))
                .leadTimeDays(body.get("leadTimeDays") != null && !body.get("leadTimeDays").isBlank()
                        ? Integer.parseInt(body.get("leadTimeDays")) : null)
                .build();
        return ResponseEntity.ok(factoryProductRepository.save(mapping));
    }

    @DeleteMapping("/{id}/products/{mappingId}")
    public ResponseEntity<?> removeMapping(@PathVariable Long id, @PathVariable Long mappingId) {
        factoryProductRepository.deleteById(mappingId);
        return ResponseEntity.ok(Map.of("message", "매핑이 삭제되었습니다."));
    }

    private Factory getFactory(Long id) {
        return factoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공장을 찾을 수 없습니다."));
    }

    private String required(String v, String label) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(label + "을(를) 입력해주세요.");
        return v;
    }

    private Integer parseInt(String v, int def) {
        try { return v == null || v.isBlank() ? def : Integer.parseInt(v); } catch (Exception e) { return def; }
    }
}
