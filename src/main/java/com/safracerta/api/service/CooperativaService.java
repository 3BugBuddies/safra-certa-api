package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.cooperativa.CooperativaRequest;
import com.safracerta.api.dto.cooperativa.PainelCooperativaResponse;
import com.safracerta.api.dto.cooperativa.PainelCooperativaResponse.DistribuicaoRisco;
import com.safracerta.api.dto.produtor.ProdutorCardResponse;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.NivelRisco;
import com.safracerta.api.repository.CooperativaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CooperativaService {

    private final CooperativaRepository repository;
    private final ProdutorService produtorService;
    private final TalhaoService talhaoService;

    public CooperativaService(CooperativaRepository repository,
                              ProdutorService produtorService,
                              TalhaoService talhaoService) {
        this.repository = repository;
        this.produtorService = produtorService;
        this.talhaoService = talhaoService;
    }

    public List<Cooperativa> listar() {
        return repository.findAll();
    }

    public Cooperativa buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Cooperativa", id));
    }

    @Transactional
    public Cooperativa criar(CooperativaRequest req) {
        if (repository.existsByCnpj(req.cnpj())) {
            throw new ConflictException("CNPJ já cadastrado: " + req.cnpj());
        }
        return repository.save(req.toEntity());
    }

    @Transactional
    public Cooperativa atualizar(Long id, CooperativaRequest req) {
        Cooperativa c = buscar(id);
        if (!c.getCnpj().equals(req.cnpj()) && repository.existsByCnpj(req.cnpj())) {
            throw new ConflictException("CNPJ já cadastrado: " + req.cnpj());
        }
        req.applyTo(c);
        return repository.save(c);
    }

    /** Cascata: remove todos os produtores da cooperativa (e tudo abaixo) antes da própria. */
    @Transactional
    public void deletar(Long id) {
        Cooperativa c = buscar(id);
        for (Produtor p : List.copyOf(c.getProdutores())) {
            produtorService.deletar(p.getId());
        }
        repository.delete(c);
    }

    // ── Visões de leitura (telas da cooperativa) ─────────────────────────────

    /** Painel agregado: contadores + distribuição de TALHÕES por nível (última análise de cada). */
    @Transactional(readOnly = true)
    public PainelCooperativaResponse painel(Long id) {
        Cooperativa coop = buscar(id);
        double totalHectares = 0;
        long saudavel = 0, atencao = 0, alerta = 0, critico = 0;
        for (Produtor p : coop.getProdutores()) {
            for (Talhao t : p.getTalhoes()) {
                if (t.getAreaHa() != null) {
                    totalHectares += t.getAreaHa();
                }
                NivelRisco nivel = talhaoService.nivelAtual(t.getId());
                if (nivel == null) {
                    continue;
                }
                switch (nivel) {
                    case SAUDAVEL -> saudavel++;
                    case ATENCAO -> atencao++;
                    case ALERTA -> alerta++;
                    case CRITICO -> critico++;
                }
            }
        }
        return new PainelCooperativaResponse(
                coop.getId(), coop.getNome(), coop.getProdutores().size(), totalHectares,
                new DistribuicaoRisco(saudavel, atencao, alerta, critico));
    }

    /** Cards de produtor com agregados (lista da cooperativa). */
    @Transactional(readOnly = true)
    public List<ProdutorCardResponse> produtoresCards(Long id) {
        Cooperativa coop = buscar(id);
        return coop.getProdutores().stream().map(produtorService::cardDe).toList();
    }
}
