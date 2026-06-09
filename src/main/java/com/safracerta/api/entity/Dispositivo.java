package com.safracerta.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/** Regra "um dispositivo por talhão" é de negócio (aplicação), não constraint de modelo. */
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

    @NotBlank
    @Column(name = "CD_DISPOSITIVO", nullable = false, unique = true)
    private String codigoDispositivo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TALHAO", nullable = false)
    private Talhao talhao;

    @NotNull
    @Column(name = "AT_ATIVO", nullable = false)
    private Boolean ativo;

    @OneToMany(mappedBy = "dispositivo", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<LeituraSensor> leituras = new ArrayList<>();
}
