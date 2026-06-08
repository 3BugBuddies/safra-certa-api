package com.safracerta.api.dto.analise;

import com.safracerta.api.entity.embeddable.Medicao;
import com.safracerta.api.entity.embeddable.Previsao;
import com.safracerta.api.entity.enums.NivelRisco;

import java.util.List;

/** Contexto que o motor entrega à IA para gerar o texto (nível já decidido). */
public record ContextoAnalise(
        String culturaNome,
        NivelRisco nivel,
        List<String> fatores,
        Medicao medicao,
        Previsao previsao
) {
}
