package com.safracerta.api.dto.cultura;

import com.safracerta.api.entity.Cultura;

public record CulturaResponse(
        Long id,
        String nome,
        Integer diasAteColheita,
        Double umidadeSoloCritica,
        Double temperaturaMinCritica,
        Double temperaturaMaxCritica,
        Double chuvaMinima
) {
    public static CulturaResponse from(Cultura c) {
        return new CulturaResponse(
                c.getId(), c.getNome(), c.getDiasAteColheita(), c.getUmidadeSoloCritica(),
                c.getTemperaturaMinCritica(), c.getTemperaturaMaxCritica(), c.getChuvaMinima());
    }
}
