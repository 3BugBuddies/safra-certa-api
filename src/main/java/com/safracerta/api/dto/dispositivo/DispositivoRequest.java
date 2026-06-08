package com.safracerta.api.dto.dispositivo;

import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.Talhao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DispositivoRequest(
        @NotBlank String codigoDispositivo,
        @NotNull Long talhaoId,
        Boolean ativo
) {
    public Dispositivo toEntity(Talhao talhao) {
        Dispositivo d = new Dispositivo();
        applyTo(d, talhao);
        return d;
    }

    public void applyTo(Dispositivo d, Talhao talhao) {
        d.setCodigoDispositivo(codigoDispositivo);
        d.setTalhao(talhao);
        d.setAtivo(ativo != null ? ativo : Boolean.TRUE);
    }
}
