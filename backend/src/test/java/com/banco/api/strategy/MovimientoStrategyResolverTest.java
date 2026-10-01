package com.banco.api.strategy;

import com.banco.api.config.BancoProperties;
import com.banco.api.entity.TipoMovimiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovimientoStrategyResolverTest {

    private final CreditoStrategy credito = new CreditoStrategy();
    private final DebitoStrategy debito = new DebitoStrategy(new BancoProperties(new BigDecimal("1000")));

    @Test
    void devuelveLaEstrategiaDeCadaTipo() {
        MovimientoStrategyResolver resolver = new MovimientoStrategyResolver(List.of(credito, debito));

        assertThat(resolver.obtener(TipoMovimiento.CREDITO)).isSameAs(credito);
        assertThat(resolver.obtener(TipoMovimiento.DEBITO)).isSameAs(debito);
    }

    @Test
    void fallaSiFaltaLaEstrategiaDeUnTipo() {
        MovimientoStrategyResolver resolver = new MovimientoStrategyResolver(List.of(credito));

        assertThatThrownBy(() -> resolver.obtener(TipoMovimiento.DEBITO))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fallaSiHayDosEstrategiasParaElMismoTipo() {
        List<MovimientoStrategy> duplicadas = List.of(credito, new CreditoStrategy());

        assertThatThrownBy(() -> new MovimientoStrategyResolver(duplicadas))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CREDITO");
    }
}
