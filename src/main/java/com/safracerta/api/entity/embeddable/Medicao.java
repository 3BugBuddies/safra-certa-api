package com.safracerta.api.entity.embeddable;

import jakarta.persistence.Column;
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
public class Medicao {

    @Column(name = "NR_MED_TEMPERATURA")
    private Double temperatura;

    @Column(name = "NR_MED_UMIDADE_AR")
    private Double umidadeAr;

    @Column(name = "NR_MED_RADIACAO_SOLAR")
    private Double radiacaoSolar;

    @Column(name = "NR_MED_UMIDADE_SOLO")
    private Double umidadeSolo;
}
