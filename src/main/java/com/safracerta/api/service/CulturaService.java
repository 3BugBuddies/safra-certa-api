package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.cultura.CulturaRequest;
import com.safracerta.api.entity.Cultura;
import com.safracerta.api.repository.CulturaRepository;
import com.safracerta.api.repository.SafraTalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CulturaService {

    private final CulturaRepository repository;
    private final SafraTalhaoRepository safraTalhaoRepository;

    public CulturaService(CulturaRepository repository, SafraTalhaoRepository safraTalhaoRepository) {
        this.repository = repository;
        this.safraTalhaoRepository = safraTalhaoRepository;
    }

    public List<Cultura> listar() {
        return repository.findAll();
    }

    public Cultura buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Cultura", id));
    }

    @Transactional
    public Cultura criar(CulturaRequest req) {
        if (repository.existsByNome(req.nome())) {
            throw new ConflictException("Cultura já cadastrada: " + req.nome());
        }
        return repository.save(req.toEntity());
    }

    @Transactional
    public Cultura atualizar(Long id, CulturaRequest req) {
        Cultura c = buscar(id);
        if (!c.getNome().equals(req.nome()) && repository.existsByNome(req.nome())) {
            throw new ConflictException("Cultura já cadastrada: " + req.nome());
        }
        req.applyTo(c);
        return repository.save(c);
    }

    @Transactional
    public void deletar(Long id) {
        Cultura c = buscar(id);
        if (safraTalhaoRepository.existsByCulturaId(id)) {
            throw new ConflictException("Cultura está em uso por safras; remova-as antes.");
        }
        repository.delete(c);
    }
}
