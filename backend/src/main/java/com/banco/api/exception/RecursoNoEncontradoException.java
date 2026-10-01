package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends BancoException {

    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
