package com.safracerta.api.dto;

import com.safracerta.api.entity.Coordenada;
import jakarta.validation.constraints.NotNull;

public record CoordenadaDto(
        @NotNull Double latitude,
        @NotNull Double longitude
) {
    public static CoordenadaDto from(Coordenada c) {
        return c == null ? null : new CoordenadaDto(c.getLatitude(), c.getLongitude());
    }

    public Coordenada toEntity() {
        return new Coordenada(latitude, longitude);
    }
}
