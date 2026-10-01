package com.banco.api.reporte;

import com.banco.api.exception.SolicitudInvalidaException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum FormatoReporte {
    JSON,
    PDF;

    public static FormatoReporte desde(String valor) {
        return Arrays.stream(values())
                .filter(formato -> formato.name().equalsIgnoreCase(valor.trim()))
                .findFirst()
                .orElseThrow(() -> new SolicitudInvalidaException(
                        "Formato de reporte no soportado: " + valor + ". Valores permitidos: " + permitidos()));
    }

    private static String permitidos() {
        return Arrays.stream(values())
                .map(formato -> formato.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
}
