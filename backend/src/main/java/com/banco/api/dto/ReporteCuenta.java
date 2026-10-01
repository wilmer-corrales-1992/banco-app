package com.banco.api.dto;

import com.banco.api.entity.TipoCuenta;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resumen de una cuenta dentro del rango del reporte.
 *
 * @param saldoInicial    saldo inicial con que se abrio la cuenta
 * @param saldoDisponible saldo al cierre de la fecha final del rango
 * @param totalCreditos   suma de creditos del rango
 * @param totalDebitos    suma de debitos del rango, en valor absoluto
 */
public record ReporteCuenta(
        String numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        Boolean estado,
        BigDecimal saldoDisponible,
        BigDecimal totalCreditos,
        BigDecimal totalDebitos,
        List<ReporteMovimiento> movimientos) {
}
