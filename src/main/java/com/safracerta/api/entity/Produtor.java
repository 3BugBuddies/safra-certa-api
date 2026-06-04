package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "PRODUTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produtor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_produtor")
    @SequenceGenerator(name = "seq_produtor", sequenceName = "SEQ_PRODUTOR", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cooperativa_id", nullable = false)
    private Cooperativa cooperativa;

    @Column(nullable = false)
    private String nome;

    private String cidade;
    private String endereco;
    private String telefone;

    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    private LocalDate dataNascimento;
    private String nomePropriedade;
    private String caf;
}
