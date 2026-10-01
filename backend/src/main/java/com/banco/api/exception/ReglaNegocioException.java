package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public class ReglaNegocioException extends BancoException {

    public ReglaNegocioException(String mensaje) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, mensaje);
    }
}
