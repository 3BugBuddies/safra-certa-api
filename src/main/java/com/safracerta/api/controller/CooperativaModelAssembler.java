package com.safracerta.api.controller;

import com.safracerta.api.dto.CooperativaResponse;
import com.safracerta.api.entity.Cooperativa;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CooperativaModelAssembler
        implements RepresentationModelAssembler<Cooperativa, EntityModel<CooperativaResponse>> {

    @Override
    public EntityModel<CooperativaResponse> toModel(Cooperativa c) {
        return EntityModel.of(
                CooperativaResponse.from(c),
                linkTo(methodOn(CooperativaController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(CooperativaController.class).listar()).withRel("cooperativas"));
    }
}
