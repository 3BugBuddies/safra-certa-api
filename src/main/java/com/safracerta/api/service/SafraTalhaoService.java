package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.safra.SafraTalhaoRequest;
import com.safracerta.api.entity.Cultura;
import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.entity.enums.StatusSafra;
import com.safracerta.api.repository.CulturaRepository;
import com.safracerta.api.repository.SafraTalhaoRepository;
import com.safracerta.api.repository.TalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SafraTalhaoService {

    private final SafraTalhaoRepository repository;
    private final TalhaoRepository talhaoRepository;
    private final CulturaRepository culturaRepository;

    public SafraTalhaoService(SafraTalhaoRepository repository,
                              TalhaoRepository talhaoRepository,
                              CulturaRepository culturaRepository) {
        this.repository = repository;
        this.talhaoRepository = talhaoRepository;
        this.culturaRepository = culturaRepository;
    }

    public List<SafraTalhao> listar(Long talhaoId) {
        return talhaoId == null
                ? repository.findAll()
                : repository.findByTalhaoId(talhaoId);
    }

    public SafraTalhao buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Safra", id));
    }

    @Transactional
    public SafraTalhao criar(SafraTalhaoRequest req) {
        Talhao talhao = resolverTalhao(req.talhaoId());
        Cultura cultura = resolverCultura(req.culturaId());
        if (resultaEmAtiva(req)
                && repository.existsByTalhaoIdAndStatusSafra(req.talhaoId(), StatusSafra.ATIVA)) {
            throw new ConflictException("Talhão já possui safra ativa: " + req.talhaoId());
        }
        return repository.save(req.toEntity(talhao, cultura));
    }

    @Transactional
    public SafraTalhao atualizar(Long id, SafraTalhaoRequest req) {
        SafraTalhao s = buscar(id);
        Talhao talhao = resolverTalhao(req.talhaoId());
        Cultura cultura = resolverCultura(req.culturaId());
        if (resultaEmAtiva(req) && existeOutraSafraAtiva(req.talhaoId(), id)) {
            throw new ConflictException("Talhão já possui safra ativa: " + req.talhaoId());
        }
        req.applyTo(s, talhao, cultura);
        return repository.save(s);
    }

    /** statusSafra ausente no request equivale a ATIVA (default da entidade). */
    private boolean resultaEmAtiva(SafraTalhaoRequest req) {
        return req.statusSafra() == null || req.statusSafra() == StatusSafra.ATIVA;
    }

    private boolean existeOutraSafraAtiva(Long talhaoId, Long ignorarId) {
        return repository.findByTalhaoId(talhaoId).stream()
                .anyMatch(s -> s.getStatusSafra() == StatusSafra.ATIVA && !s.getId().equals(ignorarId));
    }

    @Transactional
    public void deletar(Long id) {
        SafraTalhao s = buscar(id);
        repository.delete(s);
    }

    private Talhao resolverTalhao(Long talhaoId) {
        return talhaoRepository.findById(talhaoId)
                .orElseThrow(() -> NotFoundException.of("Talhão", talhaoId));
    }

    private Cultura resolverCultura(Long culturaId) {
        return culturaRepository.findById(culturaId)
                .orElseThrow(() -> NotFoundException.of("Cultura", culturaId));
    }
}
