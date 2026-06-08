package com.safracerta.api.assembler;
import com.safracerta.api.controller.TalhaoController;

import com.safracerta.api.dto.talhao.TalhaoResponse;
import com.safracerta.api.entity.Talhao;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class TalhaoModelAssembler
        implements RepresentationModelAssembler<Talhao, EntityModel<TalhaoResponse>> {

    @Override
    public EntityModel<TalhaoResponse> toModel(Talhao t) {
        return EntityModel.of(
                TalhaoResponse.from(t),
                linkTo(methodOn(TalhaoController.class).buscar(t.getId())).withSelfRel(),
                linkTo(methodOn(TalhaoController.class).listar(null)).withRel("talhoes"));
    }
}
