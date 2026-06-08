package com.safracerta.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "T_SC_PRODUTOR")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Produtor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_produtor")
    @SequenceGenerator(name = "seq_produtor", sequenceName = "SEQ_T_SC_PRODUTOR", allocationSize = 1)
    @Column(name = "ID_PRODUTOR")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COOPERATIVA", nullable = false)
    private Cooperativa cooperativa;

    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Column(name = "NM_TELEFONE")
    private String telefone;

    @Column(name = "CD_CPF", nullable = false, unique = true, length = 14)
    private String cpf;

    @Column(name = "DT_NASCIMENTO")
    private LocalDate dataNascimento;

    @Column(name = "NM_PROPRIEDADE")
    private String nomePropriedade;

    @Column(name = "CD_CAF")
    private String caf;

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

    @OneToMany(mappedBy = "produtor", fetch = FetchType.LAZY)
    private List<Talhao> talhoes = new ArrayList<>();
}
