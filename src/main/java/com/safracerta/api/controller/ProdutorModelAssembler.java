package com.safracerta.api.controller;

import com.safracerta.api.dto.ProdutorResponse;
import com.safracerta.api.entity.Produtor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProdutorModelAssembler
        implements RepresentationModelAssembler<Produtor, EntityModel<ProdutorResponse>> {

    @Override
    public EntityModel<ProdutorResponse> toModel(Produtor p) {
        return EntityModel.of(
                ProdutorResponse.from(p),
                linkTo(methodOn(ProdutorController.class).buscar(p.getId())).withSelfRel(),
                linkTo(methodOn(ProdutorController.class).listar(null)).withRel("produtores"));
    }
}
