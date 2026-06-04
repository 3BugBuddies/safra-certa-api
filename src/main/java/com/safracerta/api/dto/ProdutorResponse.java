package com.safracerta.api.dto;

import com.safracerta.api.entity.Produtor;

import java.time.LocalDate;

public record ProdutorResponse(
        Long id,
        Long cooperativaId,
        String nome,
        String cidade,
        String endereco,
        String telefone,
        String cpf,
        LocalDate dataNascimento,
        String nomePropriedade,
        String caf
) {
    public static ProdutorResponse from(Produtor p) {
        return new ProdutorResponse(
                p.getId(), p.getCooperativa().getId(), p.getNome(), p.getCidade(),
                p.getEndereco(), p.getTelefone(), p.getCpf(), p.getDataNascimento(),
                p.getNomePropriedade(), p.getCaf());
    }
}
