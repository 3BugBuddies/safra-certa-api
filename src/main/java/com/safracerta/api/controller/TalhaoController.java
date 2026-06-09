package com.safracerta.api.controller;
import com.safracerta.api.assembler.TalhaoModelAssembler;
import com.safracerta.api.assembler.TalhaoSituacaoAssembler;

import com.safracerta.api.dto.talhao.TalhaoRequest;
import com.safracerta.api.dto.talhao.TalhaoResponse;
import com.safracerta.api.dto.talhao.TalhaoSituacaoResponse;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.TalhaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/talhao")
@Tag(name = "Talhões", description = "Cadastro de talhões e polígono")
public class TalhaoController {

    private final TalhaoService service;
    private final TalhaoModelAssembler assembler;
    private final TalhaoSituacaoAssembler situacaoAssembler;

    public TalhaoController(TalhaoService service, TalhaoModelAssembler assembler,
                            TalhaoSituacaoAssembler situacaoAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.situacaoAssembler = situacaoAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista talhões (opcionalmente filtrados por produtor)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TalhaoResponse.class))))
    })
    public ResponseEntity<CollectionModel<EntityModel<TalhaoResponse>>> listar(
            @RequestParam(required = false) Long produtorId) {
        List<EntityModel<TalhaoResponse>> itens = service.listar(produtorId).stream()
                .map(assembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(TalhaoController.class).listar(produtorId)).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um talhão por id", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = TalhaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<TalhaoResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id)));
    }

    @PostMapping
    @Operation(summary = "Cria um talhão (com pontos do polígono)", responses = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    content = @Content(schema = @Schema(implementation = TalhaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Produtor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<TalhaoResponse>> criar(@Valid @RequestBody TalhaoRequest req) {
        Talhao criado = service.criar(req);
        EntityModel<TalhaoResponse> model = assembler.toModel(criado);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um talhão (substitui o polígono inteiro)", responses = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = TalhaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<TalhaoResponse>> atualizar(@PathVariable Long id,
                                                                  @Valid @RequestBody TalhaoRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um talhão (bloqueado se houver safras)", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/situacao")
    @Operation(summary = "Situação atual de um talhão (cultura, última medição, nível de risco, polígono)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = TalhaoSituacaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<TalhaoSituacaoResponse>> situacao(@PathVariable Long id) {
        return ResponseEntity.ok(situacaoAssembler.toModel(service.situacao(id)));
    }
}
