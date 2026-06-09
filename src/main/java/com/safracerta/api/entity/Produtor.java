package com.safracerta.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COOPERATIVA", nullable = false)
    private Cooperativa cooperativa;

    @NotBlank
    @Column(name = "NM_NOME", nullable = false)
    private String nome;

    @Pattern(regexp = "\\d{10,11}", message = "telefone deve ter 10 ou 11 dígitos")
    @Column(name = "NM_TELEFONE")
    private String telefone;

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos")
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

    @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos")
    @Column(name = "CD_CEP")
    private String cep;

    @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas")
    @Column(name = "CD_UF")
    private String uf;

    @OneToMany(mappedBy = "produtor", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Talhao> talhoes = new ArrayList<>();
}
