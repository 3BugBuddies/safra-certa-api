package com.safracerta.api.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Previsao {
    private Double chuva;
    private Double umidadeAr;
    private Double temperatura;
    private Double temperaturaMin;
    private Double temperaturaMax;
    private Double radiacaoSolar;
    private Double umidadeSolo;
}
