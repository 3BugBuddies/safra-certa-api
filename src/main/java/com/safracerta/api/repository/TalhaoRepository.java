package com.safracerta.api.repository;

import com.safracerta.api.entity.Talhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TalhaoRepository extends JpaRepository<Talhao, Long> {
    List<Talhao> findByProdutorId(Long produtorId);
    boolean existsByProdutorId(Long produtorId);
}
