package com.safracerta.api.dto;

import com.safracerta.api.entity.Cooperativa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CooperativaRequest(
        @NotBlank String nome,
        @NotBlank @Size(min = 14, max = 18) String cnpj,
        String telefone,
        @Email String email,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String cep,
        String uf
) {
    public Cooperativa toEntity() {
        Cooperativa c = new Cooperativa();
        applyTo(c);
        return c;
    }

    public void applyTo(Cooperativa c) {
        c.setNome(nome);
        c.setCnpj(cnpj);
        c.setTelefone(telefone);
        c.setEmail(email);
        c.setLogradouro(logradouro);
        c.setNumero(numero);
        c.setBairro(bairro);
        c.setCidade(cidade);
        c.setCep(cep);
        c.setUf(uf);
    }
}
