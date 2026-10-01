package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public class RecursoDuplicadoException extends BancoException {

    public RecursoDuplicadoException(String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
    }
}
