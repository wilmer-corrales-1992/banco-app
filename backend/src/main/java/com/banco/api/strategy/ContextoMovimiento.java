package com.banco.api.strategy;

import java.math.BigDecimal;

/**
 * Datos que una estrategia necesita para validar un movimiento.
 *
 * @param saldoAnterior saldo disponible antes de aplicar el movimiento
 * @param monto         valor del movimiento, siempre positivo
 * @param debitosDelDia total ya debitado ese dia en la cuenta, en positivo
 */
public record ContextoMovimiento(BigDecimal saldoAnterior, BigDecimal monto, BigDecimal debitosDelDia) {
}
