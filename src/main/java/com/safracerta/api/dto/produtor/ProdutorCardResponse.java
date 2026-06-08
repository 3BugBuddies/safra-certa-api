package com.safracerta.api.dto.produtor;

import com.safracerta.api.entity.enums.NivelRisco;

/**
 * Card de produtor com agregados (lista de produtores da cooperativa).
 * {@code nivelAgregado} = pior nível entre os talhões do produtor (Decisão 5); o front rotula.
 */
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
