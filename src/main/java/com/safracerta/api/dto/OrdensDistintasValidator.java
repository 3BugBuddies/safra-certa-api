package com.safracerta.api.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class OrdensDistintasValidator implements ConstraintValidator<OrdensDistintas, TalhaoRequest> {

    @Override
    public boolean isValid(TalhaoRequest req, ConstraintValidatorContext context) {
        if (req == null || req.pontos() == null || req.pontos().isEmpty()) {
            return true;
        }
        long total = req.pontos().stream()
                .map(TalhaoPontoDto::ordem)
                .filter(Objects::nonNull)
                .count();
        long distintas = req.pontos().stream()
                .map(TalhaoPontoDto::ordem)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        return total == distintas;
    }
}
