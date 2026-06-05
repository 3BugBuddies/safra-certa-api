package com.safracerta.api.controller;

import com.safracerta.api.dto.LeituraRequest;
import com.safracerta.api.dto.LeituraResponse;
import com.safracerta.api.entity.LeituraSensor;
import com.safracerta.api.service.LeituraSensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leitura")
@Tag(name = "Leituras", description = "Ingestão de leituras do sensor ESP32")
public class LeituraSensorController {

    private final LeituraSensorService service;

    public LeituraSensorController(LeituraSensorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Recebe uma leitura do ESP32 (valida dispositivo e faixa física)")
    public LeituraResponse ingerir(@Valid @RequestBody LeituraRequest req) {
        return LeituraResponse.from(service.ingerir(req));
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de leituras de um talhão")
    public List<LeituraResponse> listar(@RequestParam Long talhaoId) {
        return service.listar(talhaoId).stream().map(LeituraResponse::from).toList();
    }
}
