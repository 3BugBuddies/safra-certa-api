package com.safracerta.api.entity;
import com.safracerta.api.entity.embeddable.Coordenada;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "TALHAO_PONTO",
    uniqueConstraints = @UniqueConstraint(name = "uk_talhao_ponto_ordem", columnNames = {"talhao_id", "ordem"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TalhaoPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_talhao_ponto")
    @SequenceGenerator(name = "seq_talhao_ponto", sequenceName = "SEQ_TALHAO_PONTO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talhao_id", nullable = false)
    private Talhao talhao;

    @Column(nullable = false)
    private Integer ordem;

    @Embedded
    private Coordenada coordenada;
}
