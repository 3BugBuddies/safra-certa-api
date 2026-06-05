package com.safracerta.api.client.openmeteo;

import com.safracerta.api.entity.PrevisaoClimatica;
import com.safracerta.api.entity.Talhao;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Traduz a {@link OpenMeteoResponse} crua para a entidade {@link PrevisaoClimatica}
 * (um ponto D+1). Temperatura (média/min/máx) e chuva (soma) vêm do agregado
 * diário; umidade do ar/solo e radiação vêm do horário, média sobre as 24h de
 * D+1 (sem agregação diária na API). Unidades alinhadas ao sensor: temperatura
 * °C, umidade do ar %, radiação W/m². A umidade do solo da Open-Meteo vem em
 * m³/m³ (0–1) e é convertida para % (×100) para bater com a leitura do sensor e
 * o limiar {@code Cultura.umidadeSoloCritica}. Ver Decisão 4 do PRD 03.
 */
@Component
public class OpenMeteoMapper {

    private static final int D1 = 1; // índice do dia seguinte (forecast_days=2)

    public Optional<PrevisaoClimatica> toEntity(OpenMeteoResponse resp, Talhao talhao) {
        if (resp == null || resp.daily() == null || resp.daily().time() == null
                || resp.daily().time().size() < 2) {
            return Optional.empty();
        }
        LocalDate amanha = LocalDate.now().plusDays(1);

        PrevisaoClimatica p = new PrevisaoClimatica();
        p.setTalhao(talhao);
        p.setDataHora(LocalDateTime.now());
        p.setDataHoraPrevista(amanha.atStartOfDay());

        OpenMeteoResponse.Daily d = resp.daily();
        p.setTemperatura(at(d.temperaturaMedia(), D1));
        p.setTemperaturaMin(at(d.temperaturaMin(), D1));
        p.setTemperaturaMax(at(d.temperaturaMax(), D1));
        p.setChuva(at(d.precipitacao(), D1));

        OpenMeteoResponse.Hourly h = resp.hourly();
        List<String> horas = h != null ? h.time() : null;
        p.setUmidadeAr(mediaDoDia(horas, h != null ? h.umidadeAr() : null, amanha));
        // soil_moisture vem em m³/m³ (0–1) → converte para % (escala do sensor e do limiar da Cultura)
        p.setUmidadeSolo(fracaoParaPct(mediaDoDia(horas, h != null ? h.umidadeSolo() : null, amanha)));
        p.setRadiacaoSolar(mediaDoDia(horas, h != null ? h.radiacao() : null, amanha));
        return Optional.of(p);
    }

    private static Double at(List<Double> serie, int i) {
        return serie != null && serie.size() > i ? serie.get(i) : null;
    }

    private static Double fracaoParaPct(Double fracao) {
        return fracao == null ? null : fracao * 100;
    }

    /** Média dos valores horários cujas timestamps (em {@code tempos}) caem no dia {@code dia}. */
    private static Double mediaDoDia(List<String> tempos, List<Double> valores, LocalDate dia) {
        if (tempos == null || valores == null) return null;
        String prefixo = dia.toString(); // "yyyy-MM-dd" — timestamps do Open-Meteo começam assim
        double soma = 0;
        int n = 0;
        for (int i = 0; i < tempos.size() && i < valores.size(); i++) {
            if (tempos.get(i).startsWith(prefixo) && valores.get(i) != null) {
                soma += valores.get(i);
                n++;
            }
        }
        return n == 0 ? null : soma / n;
    }
}
