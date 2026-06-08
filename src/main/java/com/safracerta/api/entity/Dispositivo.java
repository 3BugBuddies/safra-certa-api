package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Dispositivo físico (ESP32) instalado num talhão. Emissor das leituras
 * de sensor. A regra "um dispositivo por talhão" é de negócio (aplicação),
 * não restrição de modelo. Ver PRD de ingestão/dispositivos.
 */
@Entity
@Table(name = "T_SC_DISPOSITIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dispositivo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_dispositivo")
    @SequenceGenerator(name = "seq_dispositivo", sequenceName = "SEQ_T_SC_DISPOSITIVO", allocationSize = 1)
    @Column(name = "ID_DISPOSITIVO")
    private Long id;

    @Column(name = "CD_DISPOSITIVO", nullable = false, unique = true)
    private String codigoDispositivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TALHAO", nullable = false)
    private Talhao talhao;

    @Column(name = "AT_ATIVO", nullable = false)
    private Boolean ativo;
}
