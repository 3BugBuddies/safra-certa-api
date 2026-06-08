package com.safracerta.api.entity;

import com.safracerta.api.entity.enums.StatusSafra;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_SC_SAFRA_TALHAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SafraTalhao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_safra_talhao")
    @SequenceGenerator(name = "seq_safra_talhao", sequenceName = "SEQ_T_SC_SAFRA_TALHAO", allocationSize = 1)
    @Column(name = "ID_SAFRA_TALHAO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_TALHAO", nullable = false)
    private Talhao talhao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CULTURA", nullable = false)
    private Cultura cultura;

    @Column(name = "DT_PLANTIO", nullable = false)
    private LocalDate dataPlantio;

    @Column(name = "DT_PREVISTA_COLHEITA")
    private LocalDate dataPrevistaColheita;

    @Enumerated(EnumType.STRING)
    @Column(name = "TP_STATUS_SAFRA", nullable = false)
    private StatusSafra statusSafra = StatusSafra.ATIVA;

    @OneToMany(mappedBy = "safraTalhao", fetch = FetchType.LAZY)
    private List<AnaliseTalhao> analises = new ArrayList<>();
}
