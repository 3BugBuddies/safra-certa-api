package com.safracerta.api.repository;

import com.safracerta.api.entity.Dispositivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DispositivoRepository extends JpaRepository<Dispositivo, Long> {
    boolean existsByCodigoDispositivo(String codigoDispositivo);
    boolean existsByTalhaoId(Long talhaoId);
    Optional<Dispositivo> findByCodigoDispositivo(String codigoDispositivo);

    @Modifying
    @Query("delete from Dispositivo d where d.talhao.id = :talhaoId")
    void deleteByTalhaoId(@Param("talhaoId") Long talhaoId);
}
