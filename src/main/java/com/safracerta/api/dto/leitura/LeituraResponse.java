package com.safracerta.api.dto.leitura;

import com.safracerta.api.entity.LeituraSensor;

import java.time.LocalDateTime;

public record LeituraResponse(
        Long id,
        Long dispositivoId,
        Long talhaoId,
        LocalDateTime dataHora,
        Double temperatura,
        Double umidadeAr,
        Double umidadeSolo,
        Double radiacaoSolar
) {
    public static LeituraResponse from(LeituraSensor l) {
        return new LeituraResponse(
                l.getId(),
                l.getDispositivo().getId(),
                l.getTalhao().getId(),
                l.getDataHora(),
                l.getTemperatura(),
                l.getUmidadeAr(),
                l.getUmidadeSolo(),
                l.getRadiacaoSolar());
    }
}
