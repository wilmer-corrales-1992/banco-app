package com.banco.api.exception;

import org.springframework.http.HttpStatus;

public abstract class BancoException extends RuntimeException {

    private final HttpStatus status;

    protected BancoException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
