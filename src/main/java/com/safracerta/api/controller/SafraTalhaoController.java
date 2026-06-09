package com.safracerta.api.controller;
import com.safracerta.api.assembler.SafraTalhaoModelAssembler;

import com.safracerta.api.dto.safra.SafraTalhaoRequest;
import com.safracerta.api.dto.safra.SafraTalhaoResponse;
import com.safracerta.api.entity.SafraTalhao;
import com.safracerta.api.handler.ErrorResponse;
import com.safracerta.api.service.SafraTalhaoService;
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
    @Operation(summary = "Lista safras (opcionalmente filtradas por talhão)", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SafraTalhaoResponse.class))))
    })
    public ResponseEntity<CollectionModel<EntityModel<SafraTalhaoResponse>>> listar(
            @RequestParam(required = false) Long talhaoId) {
        List<EntityModel<SafraTalhaoResponse>> itens = service.listar(talhaoId).stream()
                .map(assembler::toModel).toList();
        return ResponseEntity.ok(CollectionModel.of(itens,
                linkTo(methodOn(SafraTalhaoController.class).listar(talhaoId)).withSelfRel()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma safra por id", responses = {
            @ApiResponse(responseCode = "200", description = "Sucesso",
                    content = @Content(schema = @Schema(implementation = SafraTalhaoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<SafraTalhaoResponse>> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(assembler.toModel(service.buscar(id)));
    }

    @PostMapping
    @Operation(summary = "Cria uma safra", responses = {
            @ApiResponse(responseCode = "201", description = "Criada com sucesso",
                    content = @Content(schema = @Schema(implementation = SafraTalhaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Talhão ou cultura não encontrados",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<SafraTalhaoResponse>> criar(@Valid @RequestBody SafraTalhaoRequest req) {
        SafraTalhao criada = service.criar(req);
        EntityModel<SafraTalhaoResponse> model = assembler.toModel(criada);
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma safra (geral)", responses = {
            @ApiResponse(responseCode = "200", description = "Atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = SafraTalhaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<SafraTalhaoResponse>> atualizar(@PathVariable Long id,
                                                                       @Valid @RequestBody SafraTalhaoRequest req) {
        return ResponseEntity.ok(assembler.toModel(service.atualizar(id, req)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove uma safra", responses = {
            @ApiResponse(responseCode = "204", description = "Removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
