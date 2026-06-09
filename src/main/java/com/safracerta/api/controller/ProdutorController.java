package com.safracerta.api.controller;
import com.safracerta.api.assembler.ProdutorModelAssembler;
import com.safracerta.api.assembler.TalhaoSituacaoAssembler;

import com.safracerta.api.dto.produtor.ProdutorRequest;
import com.safracerta.api.dto.produtor.ProdutorResponse;
import com.safracerta.api.dto.talhao.TalhaoSituacaoResponse;
import com.safracerta.api.entity.Produtor;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.ProdutorService;
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
@RequestMapping("/produtor")
@Tag(name = "Produtores", description = "Cadastro de produtores")
public class ProdutorController {

    private final ProdutorService service;
    private final ProdutorModelAssembler assembler;
    private final TalhaoSituacaoAssembler situacaoAssembler;

    public ProdutorController(ProdutorService service, ProdutorModelAssembler assembler,
                              TalhaoSituacaoAssembler situacaoAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.situacaoAssembler = situacaoAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista produtores (opcionalmente filtrados por cooperativa)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProdutorResponse.class))))
    })
    public ResponseEntity<CollectionModel<EntityModel<ProdutorResponse>>> listar(
            @RequestParam(required = false) Long cooperativaId) {
        List<EntityModel<ProdutorResponse>> itens = service.listar(cooperativaId).stream()
                .map(assembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(ProdutorController.class).listar(cooperativaId)).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um produtor por id", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = ProdutorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ProdutorResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id)));
    }

    @PostMapping
    @Operation(summary = "Cria um produtor", responses = {
            @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProdutorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "CPF já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ProdutorResponse>> criar(@Valid @RequestBody ProdutorRequest req) {
        Produtor criado = service.criar(req);
        EntityModel<ProdutorResponse> model = assembler.toModel(criado);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um produtor", responses = {
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ProdutorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ProdutorResponse>> atualizar(@PathVariable Long id,
                                                                    @Valid @RequestBody ProdutorRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um produtor (bloqueado se houver talhões)", responses = {
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/talhoes")
    @Operation(summary = "Situação de cada talhão do produtor (mapa de talhões)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TalhaoSituacaoResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Não encontrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CollectionModel<EntityModel<TalhaoSituacaoResponse>>> talhoesSituacao(@PathVariable Long id) {
        List<EntityModel<TalhaoSituacaoResponse>> itens = service.talhoesSituacao(id).stream()
                .map(situacaoAssembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(ProdutorController.class).talhoesSituacao(id)).withSelfRel()));
    }
}
