package com.safracerta.api.repository;

import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.entity.enums.StatusSafra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SafraTalhaoRepository extends JpaRepository<SafraTalhao, Long> {
    List<SafraTalhao> findByTalhaoId(Long talhaoId);
    boolean existsByTalhaoId(Long talhaoId);
    boolean existsByCulturaId(Long culturaId);
    boolean existsByTalhaoIdAndStatusSafra(Long talhaoId, StatusSafra statusSafra);

    /** Safra ativa de um talhão (usada pelo Motor de Risco). */
    Optional<SafraTalhao> findFirstByTalhaoIdAndStatusSafra(Long talhaoId, StatusSafra statusSafra);

    @Modifying
    @Query("delete from SafraTalhao s where s.talhao.id = :talhaoId")
    void deleteByTalhaoId(@Param("talhaoId") Long talhaoId);
}
