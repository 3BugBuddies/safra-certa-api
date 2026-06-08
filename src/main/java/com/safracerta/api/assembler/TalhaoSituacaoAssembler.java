package com.safracerta.api.assembler;

import com.safracerta.api.controller.AnaliseTalhaoController;
import com.safracerta.api.controller.TalhaoController;
import com.safracerta.api.dto.talhao.TalhaoSituacaoResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TalhaoSituacaoAssembler
        implements RepresentationModelAssembler<TalhaoSituacaoResponse, EntityModel<TalhaoSituacaoResponse>> {

    @Override
    public EntityModel<TalhaoSituacaoResponse> toModel(TalhaoSituacaoResponse t) {
        return EntityModel.of(t,
                linkTo(methodOn(TalhaoController.class).situacao(t.id())).withSelfRel(),
                linkTo(methodOn(TalhaoController.class).buscar(t.id())).withRel("talhao"),
                linkTo(methodOn(AnaliseTalhaoController.class).listar(t.id())).withRel("analises"));
    }
}
