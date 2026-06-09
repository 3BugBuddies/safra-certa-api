package com.safracerta.api.controller;
import com.safracerta.api.assembler.CulturaModelAssembler;

import com.safracerta.api.dto.cultura.CulturaRequest;
import com.safracerta.api.dto.cultura.CulturaResponse;
import com.safracerta.api.entity.Cultura;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.CulturaService;
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
    @Operation(summary = "Lista todas as culturas", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CulturaResponse.class))))
    })
    public ResponseEntity<CollectionModel<EntityModel<CulturaResponse>>> listar() {
        List<EntityModel<CulturaResponse>> itens = service.listar().stream()
                .map(assembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(CulturaController.class).listar()).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma cultura por id", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = CulturaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CulturaResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id)));
    }

    @PostMapping
    @Operation(summary = "Cria uma cultura", responses = {
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CulturaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Nome já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CulturaResponse>> criar(@Valid @RequestBody CulturaRequest req) {
        Cultura criada = service.criar(req);
        EntityModel<CulturaResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma cultura", responses = {
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = CulturaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<CulturaResponse>> atualizar(@PathVariable Long id,
                                                                   @Valid @RequestBody CulturaRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma cultura (bloqueada se houver safras)", responses = {
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
