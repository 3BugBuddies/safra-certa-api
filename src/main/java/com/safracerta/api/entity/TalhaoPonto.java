package com.safracerta.api.entity;
import com.safracerta.api.entity.embeddable.Coordenada;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "T_SC_TALHAO_PONTO",
    uniqueConstraints = @UniqueConstraint(name = "uk_talhao_ponto_ordem", columnNames = {"ID_TALHAO", "NR_ORDEM"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalhaoPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_talhao_ponto")
    @SequenceGenerator(name = "seq_talhao_ponto", sequenceName = "SEQ_T_SC_TALHAO_PONTO", allocationSize = 1)
    @Column(name = "ID_TALHAO_PONTO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TALHAO", nullable = false)
    private Talhao talhao;

    @Column(name = "NR_ORDEM", nullable = false)
    private Integer ordem;

    @Embedded
    private Coordenada coordenada;
}
