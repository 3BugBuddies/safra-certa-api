package com.safracerta.api.dto.produtor;

import com.safracerta.api.entity.Produtor;

import java.time.LocalDate;

public record ProdutorResponse(
        Long id,
        Long cooperativaId,
        String nome,
        String telefone,
        String cpf,
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
    public static ProdutorResponse from(Produtor p) {
        return new ProdutorResponse(
                p.getId(), p.getCooperativa().getId(), p.getNome(), p.getTelefone(),
                p.getCpf(), p.getDataNascimento(), p.getNomePropriedade(), p.getCaf(),
                p.getLogradouro(), p.getNumero(), p.getBairro(), p.getCidade(),
                p.getCep(), p.getUf());
    }
}
