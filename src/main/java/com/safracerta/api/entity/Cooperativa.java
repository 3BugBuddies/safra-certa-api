package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "COOPERATIVA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cooperativa")
    @SequenceGenerator(name = "seq_cooperativa", sequenceName = "SEQ_COOPERATIVA", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 18)
    private String cnpj;

    private String telefone;
    private String email;

    // Endereço (campos planos)
    private String logradouro;
    private String numero;
    private String bairro;
    private String cidade;
    private String cep;
    private String uf;
}
