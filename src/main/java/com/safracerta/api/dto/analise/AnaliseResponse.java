package com.safracerta.api.dto.analise;
import com.safracerta.api.dto.previsao.PrevisaoDto;

import com.safracerta.api.entity.AnaliseTalhao;
import com.safracerta.api.entity.enums.NivelRisco;

import java.time.LocalDateTime;

public record AnaliseResponse(
        Long id,
        Long safraTalhaoId,
        Long talhaoId,
        LocalDateTime dataHoraAnalise,
        MedicaoDto medicaoAtual,
        PrevisaoDto previsaoPrevista,
        NivelRisco nivelRisco,
        String diagnostico,
        String recomendacao
) {
    public static AnaliseResponse from(AnaliseTalhao a) {
        return new AnaliseResponse(
                a.getId(),
                a.getSafraTalhao().getId(),
                a.getSafraTalhao().getTalhao().getId(),
                a.getDataHoraAnalise(),
                MedicaoDto.from(a.getMedicaoAtual()),
                PrevisaoDto.from(a.getPrevisaoPrevista()),
                a.getNivelRisco(),
                a.getDiagnostico(),
                a.getRecomendacao());
    }
}
