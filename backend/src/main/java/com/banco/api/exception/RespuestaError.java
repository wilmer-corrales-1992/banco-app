package com.banco.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

public record RespuestaError(
        LocalDateTime fecha,
        int estado,
        String error,
        String mensaje,
        String ruta,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) List<ErrorCampo> errores) {
}
