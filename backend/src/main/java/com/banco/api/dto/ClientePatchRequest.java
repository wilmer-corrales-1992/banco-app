package com.banco.api.dto;

import com.banco.api.entity.Genero;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientePatchRequest(
        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "El nombre no puede estar vacio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        Genero genero,

        @Min(value = 0, message = "La edad no puede ser negativa")
        @Max(value = 120, message = "La edad no puede superar 120")
        Integer edad,

        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "La identificacion no puede estar vacia")
        @Size(max = 20, message = "La identificacion no puede superar 20 caracteres")
        String identificacion,

        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "La direccion no puede estar vacia")
        @Size(max = 200, message = "La direccion no puede superar 200 caracteres")
        String direccion,

        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "El telefono no puede estar vacio")
        @Size(max = 20, message = "El telefono no puede superar 20 caracteres")
        String telefono,

        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "El clienteId no puede estar vacio")
        @Size(max = 30, message = "El clienteId no puede superar 30 caracteres")
        String clienteId,

        @Size(min = 4, max = 50, message = "La contrasena debe tener entre 4 y 50 caracteres")
        String contrasena,

        Boolean estado) {
}
