package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ALERTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_alerta")
    @SequenceGenerator(name = "seq_alerta", sequenceName = "SEQ_ALERTA", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analise_talhao_id", nullable = false, unique = true)
    private AnaliseTalhao analiseTalhao;
}
