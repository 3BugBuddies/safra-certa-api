package com.safracerta.api.controller;
import com.safracerta.api.assembler.CooperativaModelAssembler;
import com.safracerta.api.assembler.PainelCooperativaAssembler;
import com.safracerta.api.assembler.ProdutorCardAssembler;

import com.safracerta.api.dto.cooperativa.CooperativaRequest;
import com.safracerta.api.dto.cooperativa.CooperativaResponse;
import com.safracerta.api.dto.cooperativa.PainelCooperativaResponse;
import com.safracerta.api.dto.produtor.ProdutorCardResponse;
import com.safracerta.api.entity.Cooperativa;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.CooperativaService;
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
@RequestMapping("/cooperativa")
@Tag(name = "Cooperativas", description = "Cadastro de cooperativas")
public class CooperativaController {

    private final CooperativaService service;
    private final CooperativaModelAssembler assembler;
    private final PainelCooperativaAssembler painelAssembler;
    private final ProdutorCardAssembler produtorCardAssembler;

    public CooperativaController(CooperativaService service, CooperativaModelAssembler assembler,
                                 PainelCooperativaAssembler painelAssembler,
                                 ProdutorCardAssembler produtorCardAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.painelAssembler = painelAssembler;
        this.produtorCardAssembler = produtorCardAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista todas as cooperativas", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CooperativaResponse.class))))
    })
    public ResponseEntity<CollectionModel<EntityModel<CooperativaResponse>>> listar() {
        List<EntityModel<CooperativaResponse>> itens = service.listar().stream()
                .map(assembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(CooperativaController.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cooperativa por id", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = CooperativaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CooperativaResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id)));
    }

    @PostMapping
    @Operation(summary = "Cria uma cooperativa", responses = {
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CooperativaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CNPJ já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CooperativaResponse>> criar(@Valid @RequestBody CooperativaRequest req) {
        Cooperativa criada = service.criar(req);
        EntityModel<CooperativaResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cooperativa", responses = {
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CooperativaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CooperativaResponse>> atualizar(@PathVariable Long id,
                                                                       @Valid @RequestBody CooperativaRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cooperativa (bloqueada se houver produtores)", responses = {
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/painel")
    @Operation(summary = "Painel agregado da cooperativa (contadores + distribuição de risco por talhão)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = PainelCooperativaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<PainelCooperativaResponse>> painel(@PathVariable Long id) {
        return ResponseEntity.ok(painelAssembler.toModel(service.painel(id)));
    }

    @GetMapping("/{id}/produtores")
    @Operation(summary = "Cards de produtor com agregados (área, nº talhões, nº em risco, nível agregado)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProdutorCardResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CollectionModel<EntityModel<ProdutorCardResponse>>> produtoresCards(@PathVariable Long id) {
        List<EntityModel<ProdutorCardResponse>> itens = service.produtoresCards(id).stream()
                .map(produtorCardAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(CooperativaController.class).produtoresCards(id)).withSelfRel()));
    }
}
