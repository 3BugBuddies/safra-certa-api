package com.safracerta.api.dto.talhao;

import com.safracerta.api.entity.embeddable.Coordenada;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CoordenadaDto(
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {
    public static CoordenadaDto from(Coordenada c) {
        return c == null ? null : new CoordenadaDto(c.getLatitude(), c.getLongitude());
    }

    public Coordenada toEntity() {
        return new Coordenada(latitude, longitude);
    }
}
