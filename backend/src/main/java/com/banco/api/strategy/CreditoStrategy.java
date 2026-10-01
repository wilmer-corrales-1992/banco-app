package com.banco.api.strategy;

import com.banco.api.entity.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class CreditoStrategy implements MovimientoStrategy {

    @Override
    public TipoMovimiento tipo() {
        return TipoMovimiento.CREDITO;
    }

    @Override
    public void validar(ContextoMovimiento contexto) {
        // Un deposito no tiene restricciones de saldo ni de cupo.
    }

    @Override
    public BigDecimal valorConSigno(BigDecimal monto) {
        return monto.abs();
    }
}
