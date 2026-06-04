package com.safracerta.api.controller;

import com.safracerta.api.dto.SafraTalhaoResponse;
import com.safracerta.api.entity.SafraTalhao;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class SafraTalhaoModelAssembler
        implements RepresentationModelAssembler<SafraTalhao, EntityModel<SafraTalhaoResponse>> {

    @Override
    public EntityModel<SafraTalhaoResponse> toModel(SafraTalhao s) {
        return EntityModel.of(
                SafraTalhaoResponse.from(s),
                linkTo(methodOn(SafraTalhaoController.class).buscar(s.getId())).withSelfRel(),
                linkTo(methodOn(SafraTalhaoController.class).listar(null)).withRel("safras"));
    }
}
