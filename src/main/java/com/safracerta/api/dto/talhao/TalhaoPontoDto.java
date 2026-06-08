package com.safracerta.api.dto.talhao;

import com.safracerta.api.entity.embeddable.Coordenada;
import com.safracerta.api.entity.TalhaoPonto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TalhaoPontoDto(
        @NotNull @PositiveOrZero Integer ordem,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude
) {
    public static TalhaoPontoDto from(TalhaoPonto p) {
        Coordenada c = p.getCoordenada();
        return new TalhaoPontoDto(
                p.getOrdem(),
                c != null ? c.getLatitude() : null,
                c != null ? c.getLongitude() : null);
    }
}
