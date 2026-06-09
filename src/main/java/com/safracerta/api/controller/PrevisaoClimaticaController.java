package com.safracerta.api.controller;

import com.safracerta.api.dto.previsao.PrevisaoResponse;
import com.safracerta.api.service.PrevisaoClimaticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Lista o histórico de previsões de um talhão", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PrevisaoResponse.class))))
    })
    public ResponseEntity<List<PrevisaoResponse>> listar(@RequestParam Long talhaoId) {
        return ResponseEntity.ok(service.listar(talhaoId).stream().map(PrevisaoResponse::from).toList());
    }
}
