package com.safracerta.api.dto.leitura;

import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.LeituraSensor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/** dataHora é carimbado pelo servidor em {@code toEntity()} — o relógio do sensor não é fonte de verdade. */
public record LeituraRequest(
        @NotBlank String codigoDispositivo,
        @NotNull @DecimalMin("-50.0") @DecimalMax("60.0") Double temperatura,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double umidadeAr,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double umidadeSolo,
        @NotNull @DecimalMin("0.0") @DecimalMax("1500.0") Double radiacaoSolar
) {
    public LeituraSensor toEntity(Dispositivo dispositivo) {
        LeituraSensor l = new LeituraSensor();
        l.setDispositivo(dispositivo);
        l.setTalhao(dispositivo.getTalhao());
        l.setDataHora(LocalDateTime.now());
        l.setTemperatura(temperatura);
        l.setUmidadeAr(umidadeAr);
        l.setUmidadeSolo(umidadeSolo);
        l.setRadiacaoSolar(radiacaoSolar);
        return l;
    }
}
