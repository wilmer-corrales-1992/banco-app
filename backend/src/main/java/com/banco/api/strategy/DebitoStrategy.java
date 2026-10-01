package com.banco.api.strategy;

import com.banco.api.config.BancoProperties;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.CupoDiarioExcedidoException;
import com.banco.api.exception.SaldoNoDisponibleException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DebitoStrategy implements MovimientoStrategy {

    private final BancoProperties propiedades;

    public DebitoStrategy(BancoProperties propiedades) {
        this.propiedades = propiedades;
    }

    @Override
    public TipoMovimiento tipo() {
        return TipoMovimiento.DEBITO;
    }

    @Override
    public void validar(ContextoMovimiento contexto) {
        BigDecimal saldo = contexto.saldoAnterior();
        if (saldo.signum() <= 0 || saldo.compareTo(contexto.monto()) < 0) {
            throw new SaldoNoDisponibleException();
        }
        BigDecimal totalDelDia = contexto.debitosDelDia().add(contexto.monto());
        if (totalDelDia.compareTo(propiedades.limiteDiarioRetiro()) > 0) {
            throw new CupoDiarioExcedidoException();
        }
    }

    @Override
    public BigDecimal valorConSigno(BigDecimal monto) {
        return monto.abs().negate();
    }
}
