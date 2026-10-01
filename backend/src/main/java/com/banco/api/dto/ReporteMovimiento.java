package com.banco.api.dto;

import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una fila del estado de cuenta.
 *
 * @param saldoInicial    saldo de la cuenta justo antes de este movimiento
 * @param movimiento      valor con signo: positivo para creditos y negativo para debitos
 * @param saldoDisponible saldo de la cuenta despues de este movimiento
 */
public record ReporteMovimiento(
        LocalDateTime fecha,
        String cliente,
        String numeroCuenta,
        TipoCuenta tipo,
        BigDecimal saldoInicial,
        Boolean estado,
        TipoMovimiento tipoMovimiento,
        BigDecimal movimiento,
        BigDecimal saldoDisponible) {
}
