package com.banco.api.strategy;

import com.banco.api.config.BancoProperties;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.CupoDiarioExcedidoException;
import com.banco.api.exception.SaldoNoDisponibleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DebitoStrategyTest {

    private final DebitoStrategy estrategia = new DebitoStrategy(new BancoProperties(new BigDecimal("1000")));

    @Test
    void tipoEsDebito() {
        assertThat(estrategia.tipo()).isEqualTo(TipoMovimiento.DEBITO);
    }

    @Test
    void valorConSignoSiempreEsNegativo() {
        assertThat(estrategia.valorConSigno(new BigDecimal("575"))).isEqualByComparingTo("-575");
        assertThat(estrategia.valorConSigno(new BigDecimal("-575"))).isEqualByComparingTo("-575");
    }

    @Test
    void conSaldoCeroLanzaSaldoNoDisponible() {
        ContextoMovimiento contexto = contexto("0.00", "10", "0");

        assertThatThrownBy(() -> estrategia.validar(contexto))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
    }

    @Test
    void conSaldoMenorAlMontoLanzaSaldoNoDisponible() {
        ContextoMovimiento contexto = contexto("100.00", "100.01", "0");

        assertThatThrownBy(() -> estrategia.validar(contexto))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
    }

    @Test
    void siSuperaElCupoDiarioLanzaCupoDiarioExcedido() {
        ContextoMovimiento contexto = contexto("2000.00", "500", "600");

        assertThatThrownBy(() -> estrategia.validar(contexto))
                .isInstanceOf(CupoDiarioExcedidoException.class)
                .hasMessage("Cupo diario excedido");
    }

    @Test
    void permiteRetirarHastaElCupoExacto() {
        ContextoMovimiento contexto = contexto("2000.00", "400", "600");

        assertThatCode(() -> estrategia.validar(contexto)).doesNotThrowAnyException();
    }

    @Test
    void permiteRetirarTodoElSaldo() {
        ContextoMovimiento contexto = contexto("540.00", "540", "0");

        assertThatCode(() -> estrategia.validar(contexto)).doesNotThrowAnyException();
    }

    @Test
    void elSaldoSeValidaAntesQueElCupo() {
        ContextoMovimiento contexto = contexto("0.00", "2000", "0");

        assertThatThrownBy(() -> estrategia.validar(contexto)).isInstanceOf(SaldoNoDisponibleException.class);
    }

    private static ContextoMovimiento contexto(String saldo, String monto, String debitosDelDia) {
        return new ContextoMovimiento(new BigDecimal(saldo), new BigDecimal(monto), new BigDecimal(debitosDelDia));
    }
}
