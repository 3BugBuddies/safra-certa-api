package com.safracerta.api.dto.talhao;

import com.safracerta.api.entity.enums.NivelRisco;

import java.util.List;

/**
 * Situação atual de um talhão (mapa de talhões / detalhe).
 * Cultura vem da safra ATIVA; medição/nível vêm da última análise. Campos nulos = "sem dado".
 */
public record TalhaoSituacaoResponse(
        Long id,
        String nome,
        Double areaHa,
        CoordenadaDto centro,
        List<TalhaoPontoDto> pontos,
        String culturaNome,
        Double umidadeSolo,
        Double temperatura,
        NivelRisco nivelRisco
) {}
