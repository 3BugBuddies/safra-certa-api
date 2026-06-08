package com.safracerta.api.dto.cooperativa;

import com.safracerta.api.entity.Cooperativa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CooperativaRequest(
        @NotBlank String nome,
        @NotBlank @Pattern(regexp = "\\d{14}", message = "CNPJ deve ter 14 dígitos") String cnpj,
        @Pattern(regexp = "\\d{10,11}", message = "telefone deve ter 10 ou 11 dígitos") String telefone,
        @Email String email,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        @Pattern(regexp = "\\d{8}", message = "CEP deve ter 8 dígitos") String cep,
        @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas") String uf
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
