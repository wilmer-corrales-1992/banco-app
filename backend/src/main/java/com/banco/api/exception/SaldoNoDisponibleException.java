package com.banco.api.exception;

public class SaldoNoDisponibleException extends ReglaNegocioException {

    public static final String MENSAJE = "Saldo no disponible";

    public SaldoNoDisponibleException() {
        super(MENSAJE);
    }
}
