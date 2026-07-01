package com.knature.bo.service;

import com.knature.common.domain.product.*;
import com.knature.common.repository.ProductCategoryRepository;
import com.knature.common.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public Page<Product> getProducts(String keyword, ProductStatus status, Pageable pageable) {
        if (status != null) {
            return productRepository.findByStatus(status, pageable);
        }
        if (keyword != null && !keyword.isBlank()) {
            return productRepository.findByNameContaining(keyword, pageable);
        }
        return productRepository.findAll(pageable);
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
    }

    public List<ProductCategory> getAllCategories() {
        return categoryRepository.findByParentIsNullOrderBySortOrder();
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void updateStatus(Long id, ProductStatus status) {
        Product product = getProduct(id);
        product.setStatus(status);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
