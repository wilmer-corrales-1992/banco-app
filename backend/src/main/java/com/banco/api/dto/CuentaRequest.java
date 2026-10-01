package com.banco.api.dto;

import com.banco.api.entity.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaRequest(
        @NotBlank(message = "El numero de cuenta es obligatorio")
        @Size(max = 20, message = "El numero de cuenta no puede superar 20 caracteres")
        @Pattern(regexp = ValidacionTexto.SOLO_DIGITOS, message = "El numero de cuenta solo admite digitos")
        String numeroCuenta,

        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipoCuenta,

        @NotNull(message = "El saldo inicial es obligatorio")
        @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El saldo inicial admite hasta 13 enteros y 2 decimales")
        BigDecimal saldoInicial,

        Boolean estado,

        @NotBlank(message = "El clienteId es obligatorio")
        @Size(max = 30, message = "El clienteId no puede superar 30 caracteres")
        String clienteId) {
}
