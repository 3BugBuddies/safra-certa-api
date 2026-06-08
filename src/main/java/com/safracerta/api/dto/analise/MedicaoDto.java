package com.safracerta.api.dto.analise;

import com.safracerta.api.entity.embeddable.Medicao;

public record MedicaoDto(
        Double temperatura,
        Double umidadeAr,
        Double radiacaoSolar,
        Double umidadeSolo
) {
    public static MedicaoDto from(Medicao m) {
        return m == null ? null
                : new MedicaoDto(m.getTemperatura(), m.getUmidadeAr(), m.getRadiacaoSolar(), m.getUmidadeSolo());
    }
}
