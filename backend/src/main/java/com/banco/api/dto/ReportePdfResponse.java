package com.banco.api.dto;

public record ReportePdfResponse(
        String nombreArchivo,
        String tipoContenido,
        String contenidoBase64) {
}
