package com.safracerta.api.entity;

import jakarta.persistence.*;
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
    @SequenceGenerator(name = "seq_registro_climatico", sequenceName = "SEQ_REGISTRO_CLIMATICO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talhao_id", nullable = false)
    private Talhao talhao;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    private Double temperatura;
    private Double umidadeAr;
    private Double umidadeSolo;
    private Double radiacaoSolar;
}
