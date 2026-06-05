package com.safracerta.api.controller;

import com.safracerta.api.dto.PrevisaoResponse;
import com.safracerta.api.service.PrevisaoClimaticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/previsao")
@Tag(name = "Previsões", description = "Histórico de previsões climáticas (Open-Meteo) por talhão")
public class PrevisaoClimaticaController {

    private final PrevisaoClimaticaService service;

    public PrevisaoClimaticaController(PrevisaoClimaticaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de previsões de um talhão")
    public List<PrevisaoResponse> listar(@RequestParam Long talhaoId) {
        return service.listar(talhaoId).stream().map(PrevisaoResponse::from).toList();
    }
}
