package com.safracerta.api.assembler;
import com.safracerta.api.controller.DispositivoController;

import com.safracerta.api.dto.dispositivo.DispositivoResponse;
import com.safracerta.api.entity.Dispositivo;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DispositivoModelAssembler
        implements RepresentationModelAssembler<Dispositivo, EntityModel<DispositivoResponse>> {

    @Override
    public EntityModel<DispositivoResponse> toModel(Dispositivo d) {
        return EntityModel.of(
                DispositivoResponse.from(d),
                linkTo(methodOn(DispositivoController.class).buscar(d.getId())).withSelfRel(),
                linkTo(methodOn(DispositivoController.class).listar()).withRel("dispositivos"));
    }
}
