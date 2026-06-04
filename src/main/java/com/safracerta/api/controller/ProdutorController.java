package com.safracerta.api.controller;

import com.safracerta.api.dto.ProdutorRequest;
import com.safracerta.api.dto.ProdutorResponse;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.service.ProdutorService;
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
@RequestMapping("/produtores")
@Tag(name = "Produtores", description = "Cadastro de produtores")
public class ProdutorController {

    private final ProdutorService service;
    private final ProdutorModelAssembler assembler;

    public ProdutorController(ProdutorService service, ProdutorModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista produtores (opcionalmente filtrados por cooperativa)")
    public CollectionModel<EntityModel<ProdutorResponse>> listar(
            @RequestParam(required = false) Long cooperativaId) {
        List<EntityModel<ProdutorResponse>> itens = service.listar(cooperativaId).stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(ProdutorController.class).listar(cooperativaId)).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produtor por id")
    public EntityModel<ProdutorResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cria um produtor")
    public ResponseEntity<EntityModel<ProdutorResponse>> criar(@Valid @RequestBody ProdutorRequest req) {
        Produtor criado = service.criar(req);
        EntityModel<ProdutorResponse> model = assembler.toModel(criado);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produtor")
    public EntityModel<ProdutorResponse> atualizar(@PathVariable Long id,
                                                   @Valid @RequestBody ProdutorRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produtor (bloqueado se houver talhões)")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
