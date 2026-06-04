package com.safracerta.api.exception;

/** Conflito de estado: unique duplicado ou delete com dependentes → 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
