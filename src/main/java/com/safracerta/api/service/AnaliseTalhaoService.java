package com.safracerta.api.service;

import com.safracerta.api.entity.AnaliseTalhao;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.repository.AnaliseTalhaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnaliseTalhaoService {

    private final AnaliseTalhaoRepository repository;

    public AnaliseTalhaoService(AnaliseTalhaoRepository repository) {
        this.repository = repository;
    }

    public List<AnaliseTalhao> listarPorTalhao(Long talhaoId) {
        return repository.findBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(talhaoId);
    }

    public AnaliseTalhao buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> NotFoundException.of("Análise", id));
    }

    public AnaliseTalhao ultimaPorTalhao(Long talhaoId) {
        return repository.findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(talhaoId)
                .orElseThrow(() -> new NotFoundException("Nenhuma análise para o talhão " + talhaoId));
    }
}
