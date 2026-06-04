package com.safracerta.api.exception;

/** Recurso não encontrado → 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String recurso, Long id) {
        return new NotFoundException(recurso + " não encontrado(a): id " + id);
    }
}
