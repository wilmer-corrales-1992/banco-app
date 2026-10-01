package com.banco.api.dto;

import com.banco.api.entity.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * El valor se envia siempre positivo; el tipo de movimiento decide el signo con que se guarda.
 */
public record MovimientoRequest(
        @NotBlank(message = "El numero de cuenta es obligatorio")
        @Size(max = 20, message = "El numero de cuenta no puede superar 20 caracteres")
        String numeroCuenta,

        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @NotNull(message = "El valor es obligatorio")
        @DecimalMin(value = "0.01", message = "El valor debe ser mayor que cero")
        @Digits(integer = 13, fraction = 2, message = "El valor admite hasta 13 enteros y 2 decimales")
        BigDecimal valor) {
}
