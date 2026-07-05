package com.knature.fo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// scanBasePackages 에 common 포함 필수 — JpaConfig(@EnableJpaAuditing) 활성화 (createdAt/updatedAt)
@SpringBootApplication(scanBasePackages = {"com.knature.fo", "com.knature.common"})
@EntityScan(basePackages = {"com.knature.common", "com.knature.fo"})
@EnableJpaRepositories(basePackages = {"com.knature.common", "com.knature.fo"})
public class KnatureFoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnatureFoApplication.class, args);
    }
}
