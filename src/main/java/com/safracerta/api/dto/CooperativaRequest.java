package com.safracerta.api.dto;

import com.safracerta.api.entity.Cooperativa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CooperativaRequest(
        @NotBlank String nome,
        @NotBlank @Size(min = 14, max = 18) String cnpj,
        String cidade,
        String endereco,
        String estado,
        String telefone,
        @Email String email
) {
    public Cooperativa toEntity() {
        Cooperativa c = new Cooperativa();
        applyTo(c);
        return c;
    }

    public void applyTo(Cooperativa c) {
        c.setNome(nome);
        c.setCnpj(cnpj);
        c.setCidade(cidade);
        c.setEndereco(endereco);
        c.setEstado(estado);
        c.setTelefone(telefone);
        c.setEmail(email);
    }
}
