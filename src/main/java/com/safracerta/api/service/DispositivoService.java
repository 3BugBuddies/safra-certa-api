package com.safracerta.api.service;

import com.safracerta.api.dto.dispositivo.DispositivoRequest;
import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.repository.DispositivoRepository;
import com.safracerta.api.repository.LeituraSensorRepository;
import com.safracerta.api.repository.TalhaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DispositivoService {

    private final DispositivoRepository repository;
    private final TalhaoRepository talhaoRepository;
    private final LeituraSensorRepository leituraSensorRepository;

    public DispositivoService(DispositivoRepository repository, TalhaoRepository talhaoRepository, LeituraSensorRepository leituraSensorRepository) {
        this.repository = repository;
        this.talhaoRepository = talhaoRepository;
        this.leituraSensorRepository = leituraSensorRepository;
    }

    public List<Dispositivo> listar() {
        return repository.findAll();
    }

    public Dispositivo buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Dispositivo", id));
    }

    @Transactional
    public Dispositivo criar(DispositivoRequest req) {
        if (repository.existsByCodigoDispositivo(req.codigoDispositivo())) {
            throw new ConflictException("Código de dispositivo já cadastrado: " + req.codigoDispositivo());
        }
        if (repository.existsByTalhaoId(req.talhaoId())) {
            throw new ConflictException("Talhão já possui um dispositivo: " + req.talhaoId());
        }
        Talhao talhao = resolverTalhao(req.talhaoId());
        return repository.save(req.toEntity(talhao));
    }

    @Transactional
    public Dispositivo atualizar(Long id, DispositivoRequest req) {
        Dispositivo d = buscar(id);
        if (!d.getCodigoDispositivo().equals(req.codigoDispositivo())
                && repository.existsByCodigoDispositivo(req.codigoDispositivo())) {
            throw new ConflictException("Código de dispositivo já cadastrado: " + req.codigoDispositivo());
        }
        if (!d.getTalhao().getId().equals(req.talhaoId())
                && repository.existsByTalhaoId(req.talhaoId())) {
            throw new ConflictException("Talhão já possui um dispositivo: " + req.talhaoId());
        }
        Talhao talhao = resolverTalhao(req.talhaoId());
        req.applyTo(d, talhao);
        return repository.save(d);
    }

    /** Remove o dispositivo e suas leituras (sem barreira de safra ativa). */
    @Transactional
    public void deletar(Long id) {
        Dispositivo d = buscar(id);
        leituraSensorRepository.deleteByDispositivoId(id);
        repository.delete(d);
    }

    private Talhao resolverTalhao(Long talhaoId) {
        return talhaoRepository.findById(talhaoId)
                .orElseThrow(() -> NotFoundException.of("Talhão", talhaoId));
    }
}
