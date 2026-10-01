package com.banco.api.exception;

public class CupoDiarioExcedidoException extends ReglaNegocioException {

    public static final String MENSAJE = "Cupo diario excedido";

    public CupoDiarioExcedidoException() {
        super(MENSAJE);
    }
}
