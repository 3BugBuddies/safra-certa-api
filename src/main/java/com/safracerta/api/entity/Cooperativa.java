package com.safracerta.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotBlank
    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @NotBlank
    @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos")
    @Column(name = "CD_CNPJ", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Pattern(regexp = "\\d{10,11}", message = "telefone deve ter 10 ou 11 dígitos")
    @Column(name = "NM_TELEFONE")
    private String telefone;

    @Email
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

    @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos")
    @Column(name = "CD_CEP")
    private String cep;

    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
    @Column(name = "CD_UF")
    private String uf;

    @OneToMany(mappedBy = "cooperativa", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Produtor> produtores = new ArrayList<>();
}
