package com.safracerta.api.repository;

import com.safracerta.api.entity.Produtor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutorRepository extends JpaRepository<Produtor, Long> {
    List<Produtor> findByCooperativaId(Long cooperativaId);
    boolean existsByCpf(String cpf);
}
