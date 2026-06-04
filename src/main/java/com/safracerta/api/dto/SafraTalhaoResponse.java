package com.safracerta.api.dto;

import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.entity.enums.StatusSafra;

import java.time.LocalDate;

public record SafraTalhaoResponse(
        Long id,
        Long talhaoId,
        Long culturaId,
        LocalDate dataPlantio,
        LocalDate dataPrevistaColheita,
        StatusSafra statusSafra
) {
    public static SafraTalhaoResponse from(SafraTalhao s) {
        return new SafraTalhaoResponse(
                s.getId(), s.getTalhao().getId(), s.getCultura().getId(),
                s.getDataPlantio(), s.getDataPrevistaColheita(), s.getStatusSafra());
    }
}
