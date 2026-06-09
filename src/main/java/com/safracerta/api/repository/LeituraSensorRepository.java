package com.safracerta.api.repository;

import com.safracerta.api.entity.LeituraSensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {
    List<LeituraSensor> findByTalhaoIdOrderByDataHoraDesc(Long talhaoId);
}
