package com.safracerta.api.entity;
import com.safracerta.api.entity.embeddable.Previsao;
import com.safracerta.api.entity.embeddable.Medicao;

import com.safracerta.api.entity.enums.NivelRisco;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "T_SC_ANALISE_TALHAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnaliseTalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_analise_talhao")
    @SequenceGenerator(name = "seq_analise_talhao", sequenceName = "SEQ_T_SC_ANALISE_TALHAO", allocationSize = 1)
    @Column(name = "ID_ANALISE_TALHAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_SAFRA_TALHAO", nullable = false)
    private SafraTalhao safraTalhao;

    @Column(name = "DT_DATA_HORA_ANALISE", nullable = false)
    private LocalDateTime dataHoraAnalise;

    @Embedded
    private Medicao medicaoAtual;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "chuva",          column = @Column(name = "NR_PREV_CHUVA")),
        @AttributeOverride(name = "temperatura",    column = @Column(name = "NR_PREV_TEMPERATURA")),
        @AttributeOverride(name = "temperaturaMin", column = @Column(name = "NR_PREV_TEMP_MIN")),
        @AttributeOverride(name = "temperaturaMax", column = @Column(name = "NR_PREV_TEMP_MAX")),
        @AttributeOverride(name = "umidadeAr",      column = @Column(name = "NR_PREV_UMIDADE_AR")),
        @AttributeOverride(name = "radiacaoSolar",  column = @Column(name = "NR_PREV_RADIACAO_SOLAR")),
        @AttributeOverride(name = "umidadeSolo",    column = @Column(name = "NR_PREV_UMIDADE_SOLO"))
    })
    private Previsao previsaoPrevista;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_NIVEL_RISCO", nullable = false)
    private NivelRisco nivelRisco;

    @Column(name = "DS_DIAGNOSTICO", columnDefinition = "CLOB")
    private String diagnostico;

    @Column(name = "DS_RECOMENDACAO", columnDefinition = "CLOB")
    private String recomendacao;
}
