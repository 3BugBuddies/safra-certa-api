package com.safracerta.api.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Valida que os pontos do polígono não repetem o valor de {@code ordem}. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrdensDistintasValidator.class)
public @interface OrdensDistintas {
    String message() default "Os pontos do polígono têm 'ordem' duplicada.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
