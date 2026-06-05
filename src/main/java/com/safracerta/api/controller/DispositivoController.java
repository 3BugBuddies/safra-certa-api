package com.safracerta.api.controller;

import com.safracerta.api.dto.DispositivoRequest;
import com.safracerta.api.dto.DispositivoResponse;
import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.service.DispositivoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/dispositivo")
@Tag(name = "Dispositivos", description = "Dispositivos ESP32 instalados nos talhões (operador)")
public class DispositivoController {

    private final DispositivoService service;
    private final DispositivoModelAssembler assembler;

    public DispositivoController(DispositivoService service, DispositivoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista os dispositivos cadastrados")
    public CollectionModel<EntityModel<DispositivoResponse>> listar() {
        List<EntityModel<DispositivoResponse>> itens = service.listar().stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(DispositivoController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um dispositivo por id")
    public EntityModel<DispositivoResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um dispositivo")
    public ResponseEntity<EntityModel<DispositivoResponse>> criar(@Valid @RequestBody DispositivoRequest req) {
        Dispositivo criado = service.criar(req);
        EntityModel<DispositivoResponse> model = assembler.toModel(criado);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um dispositivo (inclui ativar/desativar)")
    public EntityModel<DispositivoResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody DispositivoRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um dispositivo (bloqueado enquanto houver safra ativa)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
