package com.safracerta.api.assembler;

import com.safracerta.api.controller.CooperativaController;
import com.safracerta.api.dto.cooperativa.PainelCooperativaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class PainelCooperativaAssembler
        implements RepresentationModelAssembler<PainelCooperativaResponse, EntityModel<PainelCooperativaResponse>> {

    @Override
    public EntityModel<PainelCooperativaResponse> toModel(PainelCooperativaResponse p) {
        return EntityModel.of(p,
                linkTo(methodOn(CooperativaController.class).painel(p.cooperativaId())).withSelfRel(),
                linkTo(methodOn(CooperativaController.class).produtoresCards(p.cooperativaId())).withRel("produtores"));
    }
}
