package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CULTURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cultura {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cultura")
    @SequenceGenerator(name = "seq_cultura", sequenceName = "SEQ_CULTURA", allocationSize = 1)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nome;

    private Integer diasAteColheita;
    private Double umidadeSoloCritica;
    private Double temperaturaMinCritica;
    private Double temperaturaMaxCritica;
    private Double chuvaMinima;
}
