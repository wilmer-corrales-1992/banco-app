package com.banco.api.dto;

import com.banco.api.entity.TipoCuenta;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CuentaPatchRequest(
        @Size(max = 20, message = "El numero de cuenta no puede superar 20 caracteres")
        @Pattern(regexp = ValidacionTexto.SOLO_DIGITOS, message = "El numero de cuenta solo admite digitos")
        String numeroCuenta,

        TipoCuenta tipoCuenta,

        @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El saldo inicial admite hasta 13 enteros y 2 decimales")
        BigDecimal saldoInicial,

        Boolean estado,

        @Pattern(regexp = ValidacionTexto.NO_VACIO, message = "El clienteId no puede estar vacio")
        @Size(max = 30, message = "El clienteId no puede superar 30 caracteres")
        String clienteId) {
}
