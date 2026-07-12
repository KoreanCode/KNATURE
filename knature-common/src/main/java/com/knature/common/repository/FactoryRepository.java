package com.knature.common.repository;

import com.knature.common.domain.scm.Factory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FactoryRepository extends JpaRepository<Factory, Long> {
    List<Factory> findByActiveTrueOrderByName();
}
