package com.safracerta.api.service;

import com.safracerta.api.entity.PrevisaoClimatica;
import com.safracerta.api.repository.PrevisaoClimaticaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrevisaoClimaticaService {

    private final PrevisaoClimaticaRepository repository;

    public PrevisaoClimaticaService(PrevisaoClimaticaRepository repository) {
        this.repository = repository;
    }

    public List<PrevisaoClimatica> listar(Long talhaoId) {
        return repository.findByTalhaoIdOrderByDataHoraDesc(talhaoId);
    }
}
