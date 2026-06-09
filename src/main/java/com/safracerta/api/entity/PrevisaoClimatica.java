package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_SC_PREVISAO_CLIMATICA")
@Getter
@Setter
@NoArgsConstructor
public class PrevisaoClimatica extends RegistroClimatico {

    @Column(name = "DT_HORA_PREVISTA", nullable = false)
    private LocalDateTime dataHoraPrevista;

    @Column(name = "NR_CHUVA")
    private Double chuva;

    /** Temperatura herdada = média do dia. Min/Max do D+1 para risco térmico (geada × calor). */
    @Column(name = "NR_TEMP_MIN")
    private Double temperaturaMin;

    @Column(name = "NR_TEMP_MAX")
    private Double temperaturaMax;
}
