package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.produtor.ProdutorCardResponse;
import com.safracerta.api.dto.produtor.ProdutorRequest;
import com.safracerta.api.dto.talhao.TalhaoSituacaoResponse;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.NivelRisco;
import com.safracerta.api.repository.CooperativaRepository;
import com.safracerta.api.repository.ProdutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutorService {

    private final ProdutorRepository repository;
    private final CooperativaRepository cooperativaRepository;
    private final TalhaoService talhaoService;

    public ProdutorService(ProdutorRepository repository,
                           CooperativaRepository cooperativaRepository,
                           TalhaoService talhaoService) {
        this.repository = repository;
        this.cooperativaRepository = cooperativaRepository;
        this.talhaoService = talhaoService;
    }

    public List<Produtor> listar(Long cooperativaId) {
        return cooperativaId == null
                ? repository.findAll()
                : repository.findByCooperativaId(cooperativaId);
    }

    public Produtor buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Produtor", id));
    }

    @Transactional
    public Produtor criar(ProdutorRequest req) {
        Cooperativa cooperativa = resolverCooperativa(req.cooperativaId());
        if (repository.existsByCpf(req.cpf())) {
            throw new ConflictException("CPF já cadastrado: " + req.cpf());
        }
        return repository.save(req.toEntity(cooperativa));
    }

    @Transactional
    public Produtor atualizar(Long id, ProdutorRequest req) {
        Produtor p = buscar(id);
        Cooperativa cooperativa = resolverCooperativa(req.cooperativaId());
        if (!p.getCpf().equals(req.cpf()) && repository.existsByCpf(req.cpf())) {
            throw new ConflictException("CPF já cadastrado: " + req.cpf());
        }
        req.applyTo(p, cooperativa);
        return repository.save(p);
    }

    /** Cascata: remove todos os talhões do produtor (e o que pende deles) antes do próprio. */
    @Transactional
    public void deletar(Long id) {
        Produtor p = buscar(id);
        for (Talhao t : List.copyOf(p.getTalhoes())) {
            talhaoService.deletar(t.getId());
        }
        repository.delete(p);
    }

    // ── Visões de leitura ────────────────────────────────────────────────────

    /** Situação de cada talhão do produtor (mapa de talhões). */
    @Transactional(readOnly = true)
    public List<TalhaoSituacaoResponse> talhoesSituacao(Long id) {
        Produtor p = buscar(id);
        return p.getTalhoes().stream().map(talhaoService::montarSituacao).toList();
    }

    /** Card agregado de um produtor: área total, nº talhões, nº em risco e pior nível. */
    public ProdutorCardResponse cardDe(Produtor p) {
        double areaTotal = 0;
        long emRisco = 0;
        NivelRisco pior = null;
        for (Talhao t : p.getTalhoes()) {
            if (t.getAreaHa() != null) {
                areaTotal += t.getAreaHa();
            }
            NivelRisco nivel = talhaoService.nivelAtual(t.getId());
            if (nivel == null) {
                continue;
            }
            if (nivel == NivelRisco.ALERTA || nivel == NivelRisco.CRITICO) {
                emRisco++;
            }
            if (pior == null || nivel.ordinal() > pior.ordinal()) {
                pior = nivel;
            }
        }
        return new ProdutorCardResponse(
                p.getId(), p.getNome(), p.getCidade(), p.getUf(), p.getTelefone(),
                areaTotal, p.getTalhoes().size(), emRisco, pior);
    }

    private Cooperativa resolverCooperativa(Long cooperativaId) {
        return cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> NotFoundException.of("Cooperativa", cooperativaId));
    }
}
