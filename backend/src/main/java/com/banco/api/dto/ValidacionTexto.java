package com.banco.api.dto;

final class ValidacionTexto {

    static final String NO_VACIO = "^(?!\\s*$)[\\s\\S]+$";
    static final String SOLO_DIGITOS = "\\d+";

    private ValidacionTexto() {
    }
}
