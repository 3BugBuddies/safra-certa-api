package com.safracerta.api.dto;

import com.safracerta.api.entity.Cultura;
import jakarta.validation.constraints.NotBlank;

public record CulturaRequest(
        @NotBlank String nome,
        Integer diasAteColheita,
        Double umidadeSoloCritica,
        Double temperaturaMinCritica,
        Double temperaturaMaxCritica,
        Double chuvaMinima
) {
    public Cultura toEntity() {
        Cultura c = new Cultura();
        applyTo(c);
        return c;
    }

    public void applyTo(Cultura c) {
        c.setNome(nome);
        c.setDiasAteColheita(diasAteColheita);
        c.setUmidadeSoloCritica(umidadeSoloCritica);
        c.setTemperaturaMinCritica(temperaturaMinCritica);
        c.setTemperaturaMaxCritica(temperaturaMaxCritica);
        c.setChuvaMinima(chuvaMinima);
    }
}
