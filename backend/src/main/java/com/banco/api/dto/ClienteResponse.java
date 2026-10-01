package com.banco.api.dto;

import com.banco.api.entity.Genero;

public record ClienteResponse(
        Long id,
        String clienteId,
        String nombre,
        Genero genero,
        Integer edad,
        String identificacion,
        String direccion,
        String telefono,
        Boolean estado) {
}
