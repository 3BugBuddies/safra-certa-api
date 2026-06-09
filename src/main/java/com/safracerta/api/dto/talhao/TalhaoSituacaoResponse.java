package com.safracerta.api.dto.talhao;

import com.safracerta.api.entity.enums.NivelRisco;

import java.util.List;

/** cultura da safra ATIVA; medição/nível da última análise. Campos nulos = sem dado. */
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
