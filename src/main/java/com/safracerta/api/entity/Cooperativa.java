package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_SC_COOPERATIVA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cooperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_cooperativa")
    @SequenceGenerator(name = "seq_cooperativa", sequenceName = "SEQ_T_SC_COOPERATIVA", allocationSize = 1)
    @Column(name = "ID_COOPERATIVA")
    private Long id;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "CD_CNPJ", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "NM_TELEFONE")
    private String telefone;

    @Column(name = "NM_EMAIL")
    private String email;

    @Column(name = "NM_LOGRADOURO")
    private String logradouro;

    @Column(name = "NM_NUMERO")
    private String numero;

    @Column(name = "NM_BAIRRO")
    private String bairro;

    @Column(name = "NM_CIDADE")
    private String cidade;

    @Column(name = "CD_CEP")
    private String cep;

    @Column(name = "CD_UF")
    private String uf;

    @OneToMany(mappedBy = "cooperativa", fetch = FetchType.LAZY)
    private List<Produtor> produtores = new ArrayList<>();
}
