package com.safracerta.api.entity;

import com.safracerta.api.entity.enums.NivelRisco;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ANALISE_TALHAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseTalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_analise_talhao")
    @SequenceGenerator(name = "seq_analise_talhao", sequenceName = "SEQ_ANALISE_TALHAO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safra_talhao_id", nullable = false)
    private SafraTalhao safraTalhao;

    @Column(nullable = false)
    private LocalDateTime dataHoraAnalise;

    @Embedded
    private Medicao medicaoAtual;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "chuva",         column = @Column(name = "prev_chuva")),
        @AttributeOverride(name = "temperatura",   column = @Column(name = "prev_temperatura")),
        @AttributeOverride(name = "umidadeAr",     column = @Column(name = "prev_umidade_ar")),
        @AttributeOverride(name = "radiacaoSolar", column = @Column(name = "prev_radiacao_solar")),
        @AttributeOverride(name = "umidadeSolo",   column = @Column(name = "prev_umidade_solo"))
    })
    private Previsao previsaoPrevista;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelRisco nivelRisco;

    @Column(columnDefinition = "CLOB")
    private String diagnostico;

    @Column(columnDefinition = "CLOB")
    private String recomendacao;
}
