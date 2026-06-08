package com.safracerta.api.dto.previsao;

import com.safracerta.api.entity.embeddable.Previsao;

public record PrevisaoDto(
        Double chuva,
        Double umidadeAr,
        Double temperatura,
        Double temperaturaMin,
        Double temperaturaMax,
        Double radiacaoSolar,
        Double umidadeSolo
) {
    public static PrevisaoDto from(Previsao p) {
        return p == null ? null
                : new PrevisaoDto(p.getChuva(), p.getUmidadeAr(), p.getTemperatura(),
                p.getTemperaturaMin(), p.getTemperaturaMax(), p.getRadiacaoSolar(), p.getUmidadeSolo());
    }
}
