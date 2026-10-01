package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public class SolicitudInvalidaException extends BancoException {

    public SolicitudInvalidaException(String mensaje) {
        super(HttpStatus.BAD_REQUEST, mensaje);
    }
}
