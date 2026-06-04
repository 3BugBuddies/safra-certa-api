package com.safracerta.api.dto;

import com.safracerta.api.entity.Coordenada;
import com.safracerta.api.entity.TalhaoPonto;
import jakarta.validation.constraints.NotNull;

public record TalhaoPontoDto(
        @NotNull Integer ordem,
        @NotNull Double latitude,
        @NotNull Double longitude
) {
    public static TalhaoPontoDto from(TalhaoPonto p) {
        Coordenada c = p.getCoordenada();
        return new TalhaoPontoDto(
                p.getOrdem(),
                c != null ? c.getLatitude() : null,
                c != null ? c.getLongitude() : null);
    }
}
