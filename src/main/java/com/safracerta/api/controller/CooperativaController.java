package com.safracerta.api.controller;

import com.safracerta.api.dto.CooperativaRequest;
import com.safracerta.api.dto.CooperativaResponse;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.service.CooperativaService;
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
@RequestMapping("/cooperativa")
@Tag(name = "Cooperativas", description = "Cadastro de cooperativas")
public class CooperativaController {

    private final CooperativaService service;
    private final CooperativaModelAssembler assembler;

    public CooperativaController(CooperativaService service, CooperativaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista todas as cooperativas")
    public CollectionModel<EntityModel<CooperativaResponse>> listar() {
        List<EntityModel<CooperativaResponse>> itens = service.listar().stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(CooperativaController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cooperativa por id")
    public EntityModel<CooperativaResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cria uma cooperativa")
    public ResponseEntity<EntityModel<CooperativaResponse>> criar(@Valid @RequestBody CooperativaRequest req) {
        Cooperativa criada = service.criar(req);
        EntityModel<CooperativaResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cooperativa")
    public EntityModel<CooperativaResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody CooperativaRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cooperativa (bloqueada se houver produtores)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
