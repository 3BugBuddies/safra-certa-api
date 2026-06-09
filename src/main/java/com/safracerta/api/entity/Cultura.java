package com.safracerta.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "T_SC_CULTURA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cultura {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cultura")
    @SequenceGenerator(name = "seq_cultura", sequenceName = "SEQ_T_SC_CULTURA", allocationSize = 1)
    @Column(name = "ID_CULTURA")
    private Long id;

    @NotBlank
    @Column(name = "NM_NOME", nullable = false, unique = true)
    private String nome;

    @Positive
    @Column(name = "NR_DIAS_COLHEITA")
    private Integer diasAteColheita;

    @PositiveOrZero
    @DecimalMax("100.0")
    @Column(name = "NR_UMIDADE_SOLO_CRITICA")
    private Double umidadeSoloCritica;

    @DecimalMin("-50.0")
    @Column(name = "NR_TEMP_MIN_CRITICA")
    private Double temperaturaMinCritica;

    @DecimalMax("60.0")
    @Column(name = "NR_TEMP_MAX_CRITICA")
    private Double temperaturaMaxCritica;

    @PositiveOrZero
    @Column(name = "NR_CHUVA_MINIMA")
    private Double chuvaMinima;
}
