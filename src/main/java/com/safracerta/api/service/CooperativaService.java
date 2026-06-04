package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.CooperativaRequest;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.repository.CooperativaRepository;
import com.safracerta.api.repository.ProdutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CooperativaService {

    private final CooperativaRepository repository;
    private final ProdutorRepository produtorRepository;

    public CooperativaService(CooperativaRepository repository, ProdutorRepository produtorRepository) {
        this.repository = repository;
        this.produtorRepository = produtorRepository;
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

    @Transactional
    public void deletar(Long id) {
        Cooperativa c = buscar(id);
        if (produtorRepository.existsByCooperativaId(id)) {
            throw new ConflictException("Cooperativa possui produtores vinculados; remova-os antes.");
        }
        repository.delete(c);
    }
}
