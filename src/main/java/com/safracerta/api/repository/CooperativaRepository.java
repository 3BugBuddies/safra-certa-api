package com.safracerta.api.repository;

import com.safracerta.api.entity.Cooperativa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CooperativaRepository extends JpaRepository<Cooperativa, Long> {
    boolean existsByCnpj(String cnpj);
}
