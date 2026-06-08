package com.safracerta.api.entity;
import com.safracerta.api.entity.embeddable.Coordenada;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TALHAO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Talhao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_talhao")
    @SequenceGenerator(name = "seq_talhao", sequenceName = "SEQ_TALHAO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produtor_id", nullable = false)
    private Produtor produtor;

    @Column(nullable = false)
    private String nome;

    private Double areaHa;

    @Embedded
    private Coordenada centro;

    @OneToMany(mappedBy = "talhao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<TalhaoPonto> pontos = new ArrayList<>();

    @OneToMany(mappedBy = "talhao", fetch = FetchType.LAZY)
    private List<SafraTalhao> safras = new ArrayList<>();
}
