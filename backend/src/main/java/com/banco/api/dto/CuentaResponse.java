package com.banco.api.dto;

import com.banco.api.entity.TipoCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado,
        String clienteId,
        String cliente) {
}
