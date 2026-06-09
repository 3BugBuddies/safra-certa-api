package com.safracerta.api.repository;

import com.safracerta.api.entity.AnaliseTalhao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnaliseTalhaoRepository extends JpaRepository<AnaliseTalhao, Long> {

    /** Histórico de análises de um talhão (mais recentes primeiro). Talhão derivado da safra. */
    List<AnaliseTalhao> findBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(Long talhaoId);

    /** Última análise de um talhão. */
    Optional<AnaliseTalhao> findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(Long talhaoId);

}
