package com.safracerta.api.service;

import com.safracerta.api.dto.analise.ContextoAnalise;
import com.safracerta.api.entity.AnaliseTalhao;
import com.safracerta.api.entity.Cultura;
import com.safracerta.api.entity.LeituraSensor;
import com.safracerta.api.entity.embeddable.Medicao;
import com.safracerta.api.entity.embeddable.Previsao;
import com.safracerta.api.entity.PrevisaoClimatica;
import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.NivelRisco;
import com.safracerta.api.entity.enums.StatusSafra;
import com.safracerta.api.repository.AnaliseTalhaoRepository;
import com.safracerta.api.repository.PrevisaoClimaticaRepository;
import com.safracerta.api.repository.SafraTalhaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Motor de Risco determinístico. Roda na ingestão de leitura: se o talhão tem
 * safra ATIVA, conta os fatores climáticos que cruzaram os limites da cultura e
 * registra uma {@link AnaliseTalhao} (snapshot da medição + última previsão).
 *
 * Nível por contagem de fatores: 0=SAUDAVEL, 1=ATENCAO, 2=ALERTA, 3+=CRITICO.
 * Tolera limites/dados/previsão ausentes (fator só conta se ambos existem).
 * Diagnóstico/recomendação são preenchidos pela IA (opcional, com fallback).
 */
@Service
public class MotorDeRiscoService {

    private static final Logger log = LoggerFactory.getLogger(MotorDeRiscoService.class);

    private final SafraTalhaoRepository safraTalhaoRepository;
    private final PrevisaoClimaticaRepository previsaoRepository;
    private final AnaliseTalhaoRepository analiseRepository;
    private final ObjectProvider<DiagnosticoService> diagnosticoServiceProvider;

    public MotorDeRiscoService(SafraTalhaoRepository safraTalhaoRepository,
                               PrevisaoClimaticaRepository previsaoRepository,
                               AnaliseTalhaoRepository analiseRepository,
                               ObjectProvider<DiagnosticoService> diagnosticoServiceProvider) {
        this.safraTalhaoRepository = safraTalhaoRepository;
        this.previsaoRepository = previsaoRepository;
        this.analiseRepository = analiseRepository;
        this.diagnosticoServiceProvider = diagnosticoServiceProvider;
    }

    @Transactional
    public Optional<AnaliseTalhao> avaliar(LeituraSensor leitura) {
        Talhao talhao = leitura.getTalhao();
        Optional<SafraTalhao> safraAtiva =
                safraTalhaoRepository.findFirstByTalhaoIdAndStatusSafra(talhao.getId(), StatusSafra.ATIVA);
        if (safraAtiva.isEmpty()) {
            log.debug("Talhão {} sem safra ativa; análise ignorada", talhao.getId());
            return Optional.empty();
        }
        SafraTalhao safra = safraAtiva.get();
        Cultura cultura = safra.getCultura();

        Medicao medicao = new Medicao(
                leitura.getTemperatura(), leitura.getUmidadeAr(),
                leitura.getRadiacaoSolar(), leitura.getUmidadeSolo());

        Previsao previsao = previsaoRepository
                .findFirstByTalhaoIdOrderByDataHoraDesc(talhao.getId())
                .map(this::toPrevisaoSnapshot)
                .orElse(null);

        List<String> fatores = avaliarFatores(cultura, leitura, previsao);
        NivelRisco nivel = classificar(fatores.size());

        AnaliseTalhao analise = new AnaliseTalhao();
        analise.setSafraTalhao(safra);
        analise.setDataHoraAnalise(LocalDateTime.now());
        analise.setMedicaoAtual(medicao);
        analise.setPrevisaoPrevista(previsao);
        analise.setNivelRisco(nivel);

        gerarTextoIa(cultura, nivel, fatores, medicao, previsao, analise);

        return Optional.of(analiseRepository.save(analise));
    }

    /** IA opcional: se o bean existir, preenche diagnóstico/recomendação. Falha → segue sem texto. */
    private void gerarTextoIa(Cultura cultura, NivelRisco nivel, List<String> fatores,
                              Medicao medicao, Previsao previsao, AnaliseTalhao analise) {
        DiagnosticoService diagnosticoService = diagnosticoServiceProvider.getIfAvailable();
        if (diagnosticoService == null) {
            return;
        }
        ContextoAnalise ctx = new ContextoAnalise(cultura.getNome(), nivel, fatores, medicao, previsao);
        diagnosticoService.diagnosticar(ctx).ifPresent(d -> {
            analise.setDiagnostico(d.diagnostico());
            analise.setRecomendacao(d.recomendacao());
        });
    }

    /** Fatores de risco disparados; cada um só conta se a cultura tem o limite E o dado existe. */
    private List<String> avaliarFatores(Cultura cultura, LeituraSensor leitura, Previsao previsao) {
        List<String> fatores = new ArrayList<>();

        // Solo seco (leitura do sensor)
        if (cultura.getUmidadeSoloCritica() != null && leitura.getUmidadeSolo() != null
                && leitura.getUmidadeSolo() < cultura.getUmidadeSoloCritica()) {
            fatores.add("umidade do solo abaixo do crítico (%s < %s)"
                    .formatted(leitura.getUmidadeSolo(), cultura.getUmidadeSoloCritica()));
        }
        // Geada (previsão D+1)
        if (previsao != null && cultura.getTemperaturaMinCritica() != null
                && previsao.getTemperaturaMin() != null
                && previsao.getTemperaturaMin() < cultura.getTemperaturaMinCritica()) {
            fatores.add("risco de geada (mín prevista %s < %s)"
                    .formatted(previsao.getTemperaturaMin(), cultura.getTemperaturaMinCritica()));
        }
        // Calor (previsão D+1)
        if (previsao != null && cultura.getTemperaturaMaxCritica() != null
                && previsao.getTemperaturaMax() != null
                && previsao.getTemperaturaMax() > cultura.getTemperaturaMaxCritica()) {
            fatores.add("estresse por calor (máx prevista %s > %s)"
                    .formatted(previsao.getTemperaturaMax(), cultura.getTemperaturaMaxCritica()));
        }
        // Déficit hídrico (previsão D+1)
        if (previsao != null && cultura.getChuvaMinima() != null
                && previsao.getChuva() != null
                && previsao.getChuva() < cultura.getChuvaMinima()) {
            fatores.add("déficit hídrico (chuva prevista %s < %s)"
                    .formatted(previsao.getChuva(), cultura.getChuvaMinima()));
        }
        return fatores;
    }

    private NivelRisco classificar(int fatores) {
        return switch (fatores) {
            case 0 -> NivelRisco.SAUDAVEL;
            case 1 -> NivelRisco.ATENCAO;
            case 2 -> NivelRisco.ALERTA;
            default -> NivelRisco.CRITICO;
        };
    }

    private Previsao toPrevisaoSnapshot(PrevisaoClimatica p) {
        return new Previsao(
                p.getChuva(), p.getUmidadeAr(), p.getTemperatura(),
                p.getTemperaturaMin(), p.getTemperaturaMax(),
                p.getRadiacaoSolar(), p.getUmidadeSolo());
    }
}
