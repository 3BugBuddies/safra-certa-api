package com.safracerta.api.repository;

import com.safracerta.api.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    boolean existsByCodigoDispositivo(String codigoDispositivo);
    boolean existsByTalhaoId(Long talhaoId);
    Optional<Dispositivo> findByCodigoDispositivo(String codigoDispositivo);
}
