package com.safracerta.api.repository;

import com.safracerta.api.entity.PrevisaoClimatica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrevisaoClimaticaRepository extends JpaRepository<PrevisaoClimatica, Long> {
    List<PrevisaoClimatica> findByTalhaoIdOrderByDataHoraDesc(Long talhaoId);
    Optional<PrevisaoClimatica> findFirstByTalhaoIdOrderByDataHoraDesc(Long talhaoId);
}
