package com.safracerta.api.assembler;
import com.safracerta.api.controller.TalhaoController;
import com.safracerta.api.controller.AnaliseTalhaoController;

import com.safracerta.api.dto.analise.AnaliseResponse;
import com.safracerta.api.entity.AnaliseTalhao;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AnaliseModelAssembler
        implements RepresentationModelAssembler<AnaliseTalhao, EntityModel<AnaliseResponse>> {

    @Override
    public EntityModel<AnaliseResponse> toModel(AnaliseTalhao a) {
        Long talhaoId = a.getSafraTalhao().getTalhao().getId();
        return EntityModel.of(
                AnaliseResponse.from(a),
                linkTo(methodOn(AnaliseTalhaoController.class).buscar(a.getId())).withSelfRel(),
                linkTo(methodOn(AnaliseTalhaoController.class).listar(talhaoId)).withRel("analises"),
                linkTo(methodOn(TalhaoController.class).buscar(talhaoId)).withRel("talhao"));
    }
}
