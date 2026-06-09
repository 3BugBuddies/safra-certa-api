package com.safracerta.api.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String recurso, Long id) {
        return new NotFoundException(recurso + " não encontrado(a): id " + id);
    }
}
