package com.safracerta.api.dto.previsao;

import com.safracerta.api.entity.PrevisaoClimatica;

import java.time.LocalDateTime;

public record PrevisaoResponse(
        Long id,
        Long talhaoId,
        LocalDateTime dataHora,
        LocalDateTime dataHoraPrevista,
        Double temperatura,
        Double temperaturaMin,
        Double temperaturaMax,
        Double umidadeAr,
        Double umidadeSolo,
        Double radiacaoSolar,
        Double chuva
) {
    public static PrevisaoResponse from(PrevisaoClimatica p) {
        return new PrevisaoResponse(
                p.getId(),
                p.getTalhao().getId(),
                p.getDataHora(),
                p.getDataHoraPrevista(),
                p.getTemperatura(),
                p.getTemperaturaMin(),
                p.getTemperaturaMax(),
                p.getUmidadeAr(),
                p.getUmidadeSolo(),
                p.getRadiacaoSolar(),
                p.getChuva());
    }
}
