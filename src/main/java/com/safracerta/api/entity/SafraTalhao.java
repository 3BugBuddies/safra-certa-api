package com.safracerta.api.entity;

import com.safracerta.api.entity.enums.StatusSafra;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "SAFRA_TALHAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafraTalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_safra_talhao")
    @SequenceGenerator(name = "seq_safra_talhao", sequenceName = "SEQ_SAFRA_TALHAO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "talhao_id", nullable = false)
    private Talhao talhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cultura_id", nullable = false)
    private Cultura cultura;

    @Column(nullable = false)
    private LocalDate dataPlantio;

    private LocalDate dataPrevistaColheita;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSafra statusSafra = StatusSafra.ATIVA;
}
