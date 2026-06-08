package com.safracerta.api.dto.safra;

import com.safracerta.api.entity.Cultura;
import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.StatusSafra;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SafraTalhaoRequest(
        @NotNull Long talhaoId,
        @NotNull Long culturaId,
        @NotNull LocalDate dataPlantio,
        LocalDate dataPrevistaColheita,
        StatusSafra statusSafra
) {
    public SafraTalhao toEntity(Talhao talhao, Cultura cultura) {
        SafraTalhao s = new SafraTalhao();
        applyTo(s, talhao, cultura);
        return s;
    }

    public void applyTo(SafraTalhao s, Talhao talhao, Cultura cultura) {
        s.setTalhao(talhao);
        s.setCultura(cultura);
        s.setDataPlantio(dataPlantio);
        s.setDataPrevistaColheita(dataPrevistaColheita);
        s.setStatusSafra(statusSafra != null ? statusSafra : StatusSafra.ATIVA);
    }
}
