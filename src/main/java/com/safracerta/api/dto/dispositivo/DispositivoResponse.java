package com.safracerta.api.dto.dispositivo;

import com.safracerta.api.entity.Dispositivo;

public record DispositivoResponse(
        Long id,
        String codigoDispositivo,
        Long talhaoId,
        Boolean ativo
) {
    public static DispositivoResponse from(Dispositivo d) {
        return new DispositivoResponse(
                d.getId(), d.getCodigoDispositivo(), d.getTalhao().getId(), d.getAtivo());
    }
}
