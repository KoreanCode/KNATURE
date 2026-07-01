package com.knature.bo.config;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.admin.AdminRole;
import com.knature.common.domain.product.ProductCategory;
import com.knature.common.repository.AdminRepository;
import com.knature.common.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final ProductCategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = Admin.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin1004"))
                    .name("최고관리자")
                    .role(AdminRole.SUPER_ADMIN)
                    .build();
            adminRepository.save(admin);
            log.info("기본 관리자 계정 생성: admin / admin1004");
        }

        if (categoryRepository.count() == 0) {
            categoryRepository.save(ProductCategory.builder().name("HYDRA CALMING LINE").slug("hydra-calming-line").sortOrder(1).build());
            categoryRepository.save(ProductCategory.builder().name("VOLUME LINE").slug("volume-line").sortOrder(2).build());
            categoryRepository.save(ProductCategory.builder().name("AMPOULE LINE").slug("ampoule-line").sortOrder(3).build());
            categoryRepository.save(ProductCategory.builder().name("CREAM LINE").slug("cream-line").sortOrder(4).build());
            log.info("기본 상품 카테고리 4개 생성 완료");
        }
    }
}
