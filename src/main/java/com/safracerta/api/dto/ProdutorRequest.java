package com.safracerta.api.dto;

import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.entity.Produtor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProdutorRequest(
        @NotNull Long cooperativaId,
        @NotBlank String nome,
        String telefone,
        @NotBlank @Size(min = 11, max = 14) String cpf,
        LocalDate dataNascimento,
        String nomePropriedade,
        String caf,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String cep,
        String uf
) {
    public Produtor toEntity(Cooperativa cooperativa) {
        Produtor p = new Produtor();
        applyTo(p, cooperativa);
        return p;
    }

    public void applyTo(Produtor p, Cooperativa cooperativa) {
        p.setCooperativa(cooperativa);
        p.setNome(nome);
        p.setTelefone(telefone);
        p.setCpf(cpf);
        p.setDataNascimento(dataNascimento);
        p.setNomePropriedade(nomePropriedade);
        p.setCaf(caf);
        p.setLogradouro(logradouro);
        p.setNumero(numero);
        p.setBairro(bairro);
        p.setCidade(cidade);
        p.setCep(cep);
        p.setUf(uf);
    }
}
