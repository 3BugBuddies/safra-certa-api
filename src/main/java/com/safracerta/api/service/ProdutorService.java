package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.ProdutorRequest;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.repository.CooperativaRepository;
import com.safracerta.api.repository.ProdutorRepository;
import com.safracerta.api.repository.TalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutorService {

    private final ProdutorRepository repository;
    private final CooperativaRepository cooperativaRepository;
    private final TalhaoRepository talhaoRepository;

    public ProdutorService(ProdutorRepository repository,
                           CooperativaRepository cooperativaRepository,
                           TalhaoRepository talhaoRepository) {
        this.repository = repository;
        this.cooperativaRepository = cooperativaRepository;
        this.talhaoRepository = talhaoRepository;
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

    @Transactional
    public void deletar(Long id) {
        Produtor p = buscar(id);
        if (talhaoRepository.existsByProdutorId(id)) {
            throw new ConflictException("Produtor possui talhões vinculados; remova-os antes.");
        }
        repository.delete(p);
    }

    private Cooperativa resolverCooperativa(Long cooperativaId) {
        return cooperativaRepository.findById(cooperativaId)
                .orElseThrow(() -> NotFoundException.of("Cooperativa", cooperativaId));
    }
}
