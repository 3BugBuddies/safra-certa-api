package com.safracerta.api.assembler;
import com.safracerta.api.controller.CulturaController;

import com.safracerta.api.dto.cultura.CulturaResponse;
import com.safracerta.api.entity.Cultura;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CulturaModelAssembler
        implements RepresentationModelAssembler<Cultura, EntityModel<CulturaResponse>> {

    @Override
    public EntityModel<CulturaResponse> toModel(Cultura c) {
        return EntityModel.of(
                CulturaResponse.from(c),
                linkTo(methodOn(CulturaController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(CulturaController.class).listar()).withRel("culturas"));
    }
}
