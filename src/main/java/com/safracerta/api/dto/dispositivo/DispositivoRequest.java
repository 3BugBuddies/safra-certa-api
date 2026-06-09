package com.safracerta.api.dto.dispositivo;

import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.Talhao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DispositivoRequest(
        @NotBlank String codigoDispositivo,
        @NotNull Long talhaoId
) {
    /** Nasce sempre inativo — só uma leitura real o ativa (ver LeituraSensorService.ingerir). */
    public Dispositivo toEntity(Talhao talhao) {
        Dispositivo d = new Dispositivo();
        d.setAtivo(Boolean.FALSE);
        applyTo(d, talhao);
        return d;
    }

    /** Não mexe em `ativo`: a ativação é gerida pela ingestão de leitura, não pelo CRUD. */
    public void applyTo(Dispositivo d, Talhao talhao) {
        d.setCodigoDispositivo(codigoDispositivo);
        d.setTalhao(talhao);
    }
}
