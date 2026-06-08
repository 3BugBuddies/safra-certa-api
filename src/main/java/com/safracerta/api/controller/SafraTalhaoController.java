package com.safracerta.api.controller;
import com.safracerta.api.assembler.SafraTalhaoModelAssembler;

import com.safracerta.api.dto.safra.SafraTalhaoRequest;
import com.safracerta.api.dto.safra.SafraTalhaoResponse;
import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.service.SafraTalhaoService;
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
@RequestMapping("/safra")
@Tag(name = "Safras", description = "Safras por talhão (talhão + cultura + ciclo)")
public class SafraTalhaoController {

    private final SafraTalhaoService service;
    private final SafraTalhaoModelAssembler assembler;

    public SafraTalhaoController(SafraTalhaoService service, SafraTalhaoModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista safras (opcionalmente filtradas por talhão)")
    public CollectionModel<EntityModel<SafraTalhaoResponse>> listar(
            @RequestParam(required = false) Long talhaoId) {
        List<EntityModel<SafraTalhaoResponse>> itens = service.listar(talhaoId).stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(SafraTalhaoController.class).listar(talhaoId)).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma safra por id")
    public EntityModel<SafraTalhaoResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cria uma safra")
    public ResponseEntity<EntityModel<SafraTalhaoResponse>> criar(@Valid @RequestBody SafraTalhaoRequest req) {
        SafraTalhao criada = service.criar(req);
        EntityModel<SafraTalhaoResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma safra (geral)")
    public EntityModel<SafraTalhaoResponse> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody SafraTalhaoRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma safra")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
