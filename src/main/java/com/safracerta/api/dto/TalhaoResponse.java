package com.safracerta.api.dto;

import com.safracerta.api.entity.Talhao;

import java.util.List;

public record TalhaoResponse(
        Long id,
        Long produtorId,
        String nome,
        Double areaHa,
        CoordenadaDto centro,
        List<TalhaoPontoDto> pontos
) {
    public static TalhaoResponse from(Talhao t) {
        List<TalhaoPontoDto> pontos = t.getPontos().stream()
                .map(TalhaoPontoDto::from)
                .toList();
        return new TalhaoResponse(
                t.getId(), t.getProdutor().getId(), t.getNome(), t.getAreaHa(),
                CoordenadaDto.from(t.getCentro()), pontos);
    }
}
