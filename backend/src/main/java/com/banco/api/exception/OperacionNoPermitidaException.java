package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public class OperacionNoPermitidaException extends BancoException {

    public OperacionNoPermitidaException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
