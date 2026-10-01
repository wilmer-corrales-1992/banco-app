package com.banco.api.strategy;

import com.banco.api.entity.TipoMovimiento;

import java.math.BigDecimal;

public interface MovimientoStrategy {

    TipoMovimiento tipo();

    /**
     * Lanza una excepcion de negocio si el movimiento no se puede aplicar.
     */
    void validar(ContextoMovimiento contexto);

    /**
     * Devuelve el valor con el signo que corresponde al tipo de movimiento.
     */
    BigDecimal valorConSigno(BigDecimal monto);
}
