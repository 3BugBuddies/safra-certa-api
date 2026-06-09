package com.safracerta.api.controller;

import com.safracerta.api.dto.leitura.LeituraRequest;
import com.safracerta.api.dto.leitura.LeituraResponse;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.LeituraSensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @Operation(summary = "Recebe uma leitura do ESP32 (valida dispositivo e faixa física)", responses = {
            @ApiResponse(responseCode = "201", description = "Leitura registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = LeituraResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dispositivo inválido ou faixa física fora do limite",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Dispositivo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LeituraResponse> ingerir(@Valid @RequestBody LeituraRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(LeituraResponse.from(service.ingerir(req)));
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de leituras de um talhão", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = LeituraResponse.class))))
    })
    public ResponseEntity<List<LeituraResponse>> listar(@RequestParam Long talhaoId) {
        return ResponseEntity.ok(service.listar(talhaoId).stream().map(LeituraResponse::from).toList());
    }
}
