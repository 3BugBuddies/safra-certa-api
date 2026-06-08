package com.safracerta.api.dto.produtor;

import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.entity.Produtor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record ProdutorRequest(
        @NotNull Long cooperativaId,
        @NotBlank String nome,
        @Pattern(regexp = "\\d{10,11}", message = "telefone deve ter 10 ou 11 dígitos") String telefone,
        @NotBlank @Pattern(regexp = "\\d{11}", message = "CPF deve ter 11 dígitos") String cpf,
        LocalDate dataNascimento,
        String nomePropriedade,
        String caf,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep,
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas") String uf
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
