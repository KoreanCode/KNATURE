package com.knature.bo.service;

import com.knature.common.domain.product.*;
import com.knature.common.repository.ProductCategoryRepository;
import com.knature.common.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public Page<Product> getProducts(String keyword, ProductStatus status, Long categoryId, Pageable pageable) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + keyword + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return productRepository.findAll(spec, pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public Product saveProduct(Product product) {
        // JSON 바인딩된 이미지/옵션에 역참조 연결 (cascade + orphanRemoval 저장)
        if (product.getImages() != null) {
            product.getImages().forEach(img -> img.setProduct(product));
        }
        if (product.getOptions() != null) {
            product.getOptions().forEach(opt -> opt.setProduct(product));
        }
        if (product.getId() != null) {
            // 수정: 영속 엔티티의 컬렉션을 교체해 orphanRemoval 이 확실히 동작하도록 처리
            Product existing = getProduct(product.getId());
            existing.setCategory(product.getCategory());
            existing.setName(product.getName());
            existing.setCode(product.getCode());
            existing.setPrice(product.getPrice());
            existing.setSalePrice(product.getSalePrice());
            existing.setDescription(product.getDescription());
            existing.setDetailContent(product.getDetailContent());
            if (product.getStatus() != null) existing.setStatus(product.getStatus());
            if (product.getDisplayed() != null) existing.setDisplayed(product.getDisplayed());
            if (product.getStockQuantity() != null) existing.setStockQuantity(product.getStockQuantity());
            // 교체 전략: 기존 행 전체 삭제 후 새로 삽입 (id 충돌 방지)
            existing.getImages().clear();
            if (product.getImages() != null) {
                product.getImages().forEach(img -> {
                    img.setId(null); img.setProduct(existing); existing.getImages().add(img);
                });
            }
            existing.getOptions().clear();
            if (product.getOptions() != null) {
                product.getOptions().forEach(opt -> {
                    opt.setId(null); opt.setProduct(existing); existing.getOptions().add(opt);
                });
            }
            return existing;
        }
        return productRepository.save(product);
    }

    @Transactional
    public void updateStatus(Long id, ProductStatus status) {
        Product product = getProduct(id);
        product.setStatus(status);
    }

    /** 선택 상품 판매상태 일괄 변경 (FO SOLD 배지 연동) */
    @Transactional
    public int updateStatusBulk(List<Long> ids, ProductStatus status) {
        List<Product> products = productRepository.findAllById(ids);
        products.forEach(p -> p.setStatus(status));
        return products.size();
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // ===== 상품 분류(카테고리) CRUD =====

    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder();
    }

    @Transactional
    public ProductCategory createCategory(String name, String slug, Integer sortOrder) {
        if (name == null || name.isBlank() || slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("분류명과 슬러그를 입력해주세요.");
        }
        return categoryRepository.save(ProductCategory.builder()
                .name(name).slug(slug).sortOrder(sortOrder).build());
    }

    @Transactional
    public ProductCategory updateCategory(Long id, String name, String slug, Integer sortOrder) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("분류를 찾을 수 없습니다: " + id));
        if (name != null && !name.isBlank()) category.setName(name);
        if (slug != null && !slug.isBlank()) category.setSlug(slug);
        if (sortOrder != null) category.setSortOrder(sortOrder);
        return category;
    }

    @Transactional
    public void deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("분류를 찾을 수 없습니다: " + id));
        long inUse = productRepository.countByCategoryId(id);
        if (inUse > 0) {
            throw new IllegalArgumentException("해당 분류를 사용 중인 상품이 " + inUse + "개 있어 삭제할 수 없습니다.");
        }
        categoryRepository.delete(category);
    }
}
