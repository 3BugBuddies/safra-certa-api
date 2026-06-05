package com.safracerta.api.controller;

import com.safracerta.api.dto.CulturaRequest;
import com.safracerta.api.dto.CulturaResponse;
import com.safracerta.api.entity.Cultura;
import com.safracerta.api.service.CulturaService;
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
@RequestMapping("/cultura")
@Tag(name = "Culturas", description = "Catálogo de culturas e seus thresholds")
public class CulturaController {

    private final CulturaService service;
    private final CulturaModelAssembler assembler;

    public CulturaController(CulturaService service, CulturaModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista todas as culturas")
    public CollectionModel<EntityModel<CulturaResponse>> listar() {
        List<EntityModel<CulturaResponse>> itens = service.listar().stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(CulturaController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cultura por id")
    public EntityModel<CulturaResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cria uma cultura")
    public ResponseEntity<EntityModel<CulturaResponse>> criar(@Valid @RequestBody CulturaRequest req) {
        Cultura criada = service.criar(req);
        EntityModel<CulturaResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cultura")
    public EntityModel<CulturaResponse> atualizar(@PathVariable Long id,
                                                  @Valid @RequestBody CulturaRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cultura (bloqueada se houver safras)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
