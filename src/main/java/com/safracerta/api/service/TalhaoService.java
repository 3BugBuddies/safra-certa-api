package com.safracerta.api.service;

import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.talhao.CoordenadaDto;
import com.safracerta.api.dto.talhao.TalhaoPontoDto;
import com.safracerta.api.dto.talhao.TalhaoRequest;
import com.safracerta.api.dto.talhao.TalhaoSituacaoResponse;
import com.safracerta.api.entity.AnaliseTalhao;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.NivelRisco;
import com.safracerta.api.entity.enums.StatusSafra;
import com.safracerta.api.repository.AnaliseTalhaoRepository;
import com.safracerta.api.repository.ProdutorRepository;
import com.safracerta.api.repository.SafraTalhaoRepository;
import com.safracerta.api.repository.TalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TalhaoService {

    private final TalhaoRepository repository;
    private final ProdutorRepository produtorRepository;
    private final SafraTalhaoRepository safraTalhaoRepository;
    private final AnaliseTalhaoRepository analiseTalhaoRepository;

    public TalhaoService(TalhaoRepository repository,
                         ProdutorRepository produtorRepository,
                         SafraTalhaoRepository safraTalhaoRepository,
                         AnaliseTalhaoRepository analiseTalhaoRepository) {
        this.repository = repository;
        this.produtorRepository = produtorRepository;
        this.safraTalhaoRepository = safraTalhaoRepository;
        this.analiseTalhaoRepository = analiseTalhaoRepository;
    }

    public List<Talhao> listar(Long produtorId) {
        return produtorId == null
                ? repository.findAll()
                : repository.findByProdutorId(produtorId);
    }

    public Talhao buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Talhão", id));
    }

    @Transactional
    public Talhao criar(TalhaoRequest req) {
        Produtor produtor = resolverProdutor(req.produtorId());
        return repository.save(req.toEntity(produtor));
    }

    @Transactional
    public Talhao atualizar(Long id, TalhaoRequest req) {
        Talhao t = buscar(id);
        Produtor produtor = resolverProdutor(req.produtorId());
        t.getPontos().clear();
        repository.saveAndFlush(t);
        req.applyTo(t, produtor);
        return repository.save(t);
    }

    @Transactional
    public void deletar(Long id) {
        repository.delete(buscar(id));
    }

    @Transactional(readOnly = true)
    public TalhaoSituacaoResponse situacao(Long id) {
        return montarSituacao(buscar(id));
    }

    public TalhaoSituacaoResponse montarSituacao(Talhao t) {
        String culturaNome = safraTalhaoRepository
                .findFirstByTalhaoIdAndStatusSafra(t.getId(), StatusSafra.ATIVA)
                .map(s -> s.getCultura().getNome())
                .orElse(null);

        AnaliseTalhao ultima = analiseTalhaoRepository
                .findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(t.getId())
                .orElse(null);
        Double umidadeSolo = (ultima != null && ultima.getMedicaoAtual() != null)
                ? ultima.getMedicaoAtual().getUmidadeSolo() : null;
        Double temperatura = (ultima != null && ultima.getMedicaoAtual() != null)
                ? ultima.getMedicaoAtual().getTemperatura() : null;
        NivelRisco nivel = (ultima != null) ? ultima.getNivelRisco() : null;

        List<TalhaoPontoDto> pontos = t.getPontos().stream().map(TalhaoPontoDto::from).toList();
        return new TalhaoSituacaoResponse(
                t.getId(), t.getNome(), t.getAreaHa(),
                CoordenadaDto.from(t.getCentro()), pontos,
                culturaNome, umidadeSolo, temperatura, nivel);
    }

    /** Nível da última análise do talhão; null se não houver análise. */
    public NivelRisco nivelAtual(Long talhaoId) {
        return analiseTalhaoRepository
                .findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(talhaoId)
                .map(AnaliseTalhao::getNivelRisco)
                .orElse(null);
    }

    private Produtor resolverProdutor(Long produtorId) {
        return produtorRepository.findById(produtorId)
                .orElseThrow(() -> NotFoundException.of("Produtor", produtorId));
    }
}
