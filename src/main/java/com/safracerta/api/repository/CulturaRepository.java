package com.safracerta.api.repository;

import com.safracerta.api.entity.Cultura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CulturaRepository extends JpaRepository<Cultura, Long> {
    boolean existsByNome(String nome);
}
