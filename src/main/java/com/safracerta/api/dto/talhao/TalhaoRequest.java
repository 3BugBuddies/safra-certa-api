package com.safracerta.api.dto.talhao;
import com.safracerta.api.validation.OrdensDistintas;

import com.safracerta.api.entity.embeddable.Coordenada;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.TalhaoPonto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@OrdensDistintas
public record TalhaoRequest(
        @NotNull Long produtorId,
        @NotBlank String nome,
        @PositiveOrZero Double areaHa,
        @Valid CoordenadaDto centro,
        @Valid List<TalhaoPontoDto> pontos
) {
    public Talhao toEntity(Produtor produtor) {
        Talhao t = new Talhao();
        applyTo(t, produtor);
        return t;
    }

    public void applyTo(Talhao t, Produtor produtor) {
        t.setProdutor(produtor);
        t.setNome(nome);
        t.setAreaHa(areaHa);
        t.setCentro(centro != null ? centro.toEntity() : null);
        // PUT substitui o polígono inteiro — orphanRemoval apaga os antigos.
        t.getPontos().clear();
        if (pontos != null) {
            for (TalhaoPontoDto pd : pontos) {
                TalhaoPonto tp = new TalhaoPonto();
                tp.setTalhao(t);
                tp.setOrdem(pd.ordem());
                tp.setCoordenada(new Coordenada(pd.latitude(), pd.longitude()));
                t.getPontos().add(tp);
            }
        }
    }
}
