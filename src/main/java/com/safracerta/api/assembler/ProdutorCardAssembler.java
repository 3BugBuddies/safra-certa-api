package com.safracerta.api.assembler;

import com.safracerta.api.controller.ProdutorController;
import com.safracerta.api.dto.produtor.ProdutorCardResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProdutorCardAssembler
        implements RepresentationModelAssembler<ProdutorCardResponse, EntityModel<ProdutorCardResponse>> {

    @Override
    public EntityModel<ProdutorCardResponse> toModel(ProdutorCardResponse p) {
        return EntityModel.of(p,
                linkTo(methodOn(ProdutorController.class).buscar(p.id())).withSelfRel(),
                linkTo(methodOn(ProdutorController.class).talhoesSituacao(p.id())).withRel("talhoes"));
    }
}
