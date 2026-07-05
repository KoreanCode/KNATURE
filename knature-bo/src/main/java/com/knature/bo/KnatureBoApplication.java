package com.knature.bo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.knature.bo", "com.knature.common"})
@EntityScan(basePackages = {"com.knature.common", "com.knature.bo"})
@EnableJpaRepositories(basePackages = {"com.knature.common", "com.knature.bo"})
@EnableScheduling
public class KnatureBoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KnatureBoApplication.class, args);
    }
}
