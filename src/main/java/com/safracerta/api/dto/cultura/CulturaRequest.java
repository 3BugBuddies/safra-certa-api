package com.safracerta.api.dto.cultura;

import com.safracerta.api.entity.Cultura;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CulturaRequest(
        @NotBlank String nome,
        @Positive Integer diasAteColheita,
        @PositiveOrZero @DecimalMax("100.0") Double umidadeSoloCritica,
        Double temperaturaMinCritica,   // pode ser negativa (geada) — sem restrição de sinal
        Double temperaturaMaxCritica,
        @PositiveOrZero Double chuvaMinima
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
