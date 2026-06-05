package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro climático previsto por fonte externa (Open-Meteo).
 * Herda talhão, dataHora (= momento da consulta) e grandezas de
 * {@link RegistroClimatico}; adiciona o horizonte previsto e a chuva.
 */
@Entity
@Table(name = "PREVISAO_CLIMATICA")
@Getter
@Setter
@NoArgsConstructor
public class PrevisaoClimatica extends RegistroClimatico {

    @Column(nullable = false)
    private LocalDateTime dataHoraPrevista;

    private Double chuva;

    /** Temperatura herdada = média do dia. Min/Max do D+1 para risco térmico (geada × calor). */
    private Double temperaturaMin;
    private Double temperaturaMax;
}
