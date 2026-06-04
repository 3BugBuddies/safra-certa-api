package com.safracerta.api.repository;

import com.safracerta.api.entity.SafraTalhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafraTalhaoRepository extends JpaRepository<SafraTalhao, Long> {
    List<SafraTalhao> findByTalhaoId(Long talhaoId);
    boolean existsByTalhaoId(Long talhaoId);
    boolean existsByCulturaId(Long culturaId);
}
