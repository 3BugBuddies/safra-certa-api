package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Registro climático medido por um dispositivo (ESP32) em campo.
 * Herda talhão, dataHora e grandezas de {@link RegistroClimatico}.
 */
@Entity
@Table(name = "T_SC_LEITURA_SENSOR")
@Getter
@Setter
@NoArgsConstructor
public class LeituraSensor extends RegistroClimatico {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DISPOSITIVO", nullable = false)
    private Dispositivo dispositivo;
}
