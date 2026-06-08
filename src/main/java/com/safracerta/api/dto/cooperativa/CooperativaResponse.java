package com.safracerta.api.dto.cooperativa;

import com.safracerta.api.entity.Cooperativa;

public record CooperativaResponse(
        Long id,
        String nome,
        String cnpj,
        String telefone,
        String email,
        String logradouro,
        String numero,
        String bairro,
        String cidade,
        String cep,
        String uf
) {
    public static CooperativaResponse from(Cooperativa c) {
        return new CooperativaResponse(
                c.getId(), c.getNome(), c.getCnpj(), c.getTelefone(), c.getEmail(),
                c.getLogradouro(), c.getNumero(), c.getBairro(), c.getCidade(),
                c.getCep(), c.getUf());
    }
}
