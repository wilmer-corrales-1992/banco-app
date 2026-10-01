package com.banco.api.strategy;

import com.banco.api.entity.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CreditoStrategyTest {

    private final CreditoStrategy estrategia = new CreditoStrategy();

    @Test
    void tipoEsCredito() {
        assertThat(estrategia.tipo()).isEqualTo(TipoMovimiento.CREDITO);
    }

    @Test
    void valorConSignoSiempreEsPositivo() {
        assertThat(estrategia.valorConSigno(new BigDecimal("600"))).isEqualByComparingTo("600");
        assertThat(estrategia.valorConSigno(new BigDecimal("-600"))).isEqualByComparingTo("600");
    }

    @Test
    void unDepositoNoTieneRestriccionesDeSaldoNiCupo() {
        ContextoMovimiento contexto = new ContextoMovimiento(
                BigDecimal.ZERO, new BigDecimal("5000"), new BigDecimal("1000"));

        assertThatCode(() -> estrategia.validar(contexto)).doesNotThrowAnyException();
    }
}
