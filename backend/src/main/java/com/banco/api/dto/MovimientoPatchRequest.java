package com.banco.api.dto;

import com.banco.api.entity.TipoMovimiento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record MovimientoPatchRequest(
        TipoMovimiento tipoMovimiento,

        @DecimalMin(value = "0.01", message = "El valor debe ser mayor que cero")
        @Digits(integer = 13, fraction = 2, message = "El valor admite hasta 13 enteros y 2 decimales")
        BigDecimal valor) {
}
