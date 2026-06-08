package com.safracerta.api.controller;
import com.safracerta.api.assembler.AnaliseModelAssembler;

import com.safracerta.api.dto.analise.AnaliseResponse;
import com.safracerta.api.service.AnaliseTalhaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/analise")
@Tag(name = "Análises", description = "Análises de risco do talhão (Motor de Risco + IA)")
public class AnaliseTalhaoController {

    private final AnaliseTalhaoService service;
    private final AnaliseModelAssembler assembler;

    public AnaliseTalhaoController(AnaliseTalhaoService service, AnaliseModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista o histórico de análises de um talhão (mais recentes primeiro)")
    public CollectionModel<EntityModel<AnaliseResponse>> listar(@RequestParam Long talhaoId) {
        List<EntityModel<AnaliseResponse>> itens = service.listarPorTalhao(talhaoId).stream()
                .map(assembler::toModel).toList();
        return CollectionModel.of(itens,
                linkTo(methodOn(AnaliseTalhaoController.class).listar(talhaoId)).withSelfRel());
    }

    @GetMapping("/ultima")
    @Operation(summary = "Última análise de um talhão")
    public EntityModel<AnaliseResponse> ultima(@RequestParam Long talhaoId) {
        return assembler.toModel(service.ultimaPorTalhao(talhaoId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma análise por id")
    public EntityModel<AnaliseResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }
}
