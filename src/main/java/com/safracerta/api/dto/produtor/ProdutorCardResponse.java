package com.safracerta.api.dto.produtor;

import com.safracerta.api.entity.enums.NivelRisco;

/** {@code nivelAgregado} = pior nível entre os talhões do produtor; rótulo/cor fica no front. */
public record ProdutorCardResponse(
        Long id,
        String nome,
        String cidade,
        String uf,
        String telefone,
        double areaTotalHa,
        long qtdTalhoes,
        long qtdTalhoesEmRisco,   // nível ∈ {ALERTA, CRITICO}
        NivelRisco nivelAgregado  // null se nenhum talhão tem análise
) {}
