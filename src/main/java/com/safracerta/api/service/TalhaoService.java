package com.safracerta.api.service;

import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.dto.TalhaoRequest;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.entity.Talhao;
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

    public TalhaoService(TalhaoRepository repository,
                         ProdutorRepository produtorRepository,
                         SafraTalhaoRepository safraTalhaoRepository) {
        this.repository = repository;
        this.produtorRepository = produtorRepository;
        this.safraTalhaoRepository = safraTalhaoRepository;
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
        req.applyTo(t, produtor);
        return repository.save(t);
    }

    @Transactional
    public void deletar(Long id) {
        Talhao t = buscar(id);
        if (safraTalhaoRepository.existsByTalhaoId(id)) {
            throw new ConflictException("Talhão possui safras vinculadas; remova-as antes.");
        }
        repository.delete(t);
    }

    private Produtor resolverProdutor(Long produtorId) {
        return produtorRepository.findById(produtorId)
                .orElseThrow(() -> NotFoundException.of("Produtor", produtorId));
    }
}
