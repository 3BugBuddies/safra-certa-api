package com.safracerta.api.dto;

import com.safracerta.api.entity.Cooperativa;

public record CooperativaResponse(
        Long id,
        String nome,
        String cnpj,
        String cidade,
        String endereco,
        String estado,
        String telefone,
        String email
) {
    public static CooperativaResponse from(Cooperativa c) {
        return new CooperativaResponse(
                c.getId(), c.getNome(), c.getCnpj(), c.getCidade(),
                c.getEndereco(), c.getEstado(), c.getTelefone(), c.getEmail());
    }
}
