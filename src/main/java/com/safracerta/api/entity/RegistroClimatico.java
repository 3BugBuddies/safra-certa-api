package com.safracerta.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Superclasse abstrata dos registros climáticos de um talhão num instante.
 * Estratégia TABLE_PER_CLASS: cada subclasse concreta tem sua própria tabela
 * (sem tabela base no banco). Id único global via sequence compartilhada.
 * Ver ADR 01 (.claude/docs/decisions/01-heranca-registro-climatico.md).
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public abstract class RegistroClimatico {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_registro_climatico")
    @SequenceGenerator(name = "seq_registro_climatico", sequenceName = "SEQ_T_SC_REGISTRO_CLIMATICO", allocationSize = 1)
    @Column(name = "ID_REGISTRO_CLIMATICO")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TALHAO", nullable = false)
    private Talhao talhao;

    @NotNull
    @Column(name = "DT_DATA_HORA", nullable = false)
    private LocalDateTime dataHora;

    @DecimalMin("-50.0") @DecimalMax("60.0")
    @Column(name = "NR_TEMPERATURA")
    private Double temperatura;

    @DecimalMin("0.0") @DecimalMax("100.0")
    @Column(name = "NR_UMIDADE_AR")
    private Double umidadeAr;

    @DecimalMin("0.0") @DecimalMax("100.0")
    @Column(name = "NR_UMIDADE_SOLO")
    private Double umidadeSolo;

    @DecimalMin("0.0") @DecimalMax("1500.0")
    @Column(name = "NR_RADIACAO_SOLAR")
    private Double radiacaoSolar;
}
