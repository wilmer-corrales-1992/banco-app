package com.banco.api.strategy;

import com.banco.api.entity.TipoMovimiento;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Agregar un tipo de movimiento nuevo solo requiere una estrategia nueva; este resolvedor no cambia.
 */
@Component
public class MovimientoStrategyResolver {

    private final Map<TipoMovimiento, MovimientoStrategy> estrategias;

    public MovimientoStrategyResolver(List<MovimientoStrategy> estrategias) {
        this.estrategias = estrategias.stream()
                .collect(Collectors.toMap(
                        MovimientoStrategy::tipo,
                        Function.identity(),
                        (primera, segunda) -> {
                            throw new IllegalStateException("Estrategia duplicada para " + primera.tipo());
                        },
                        () -> new EnumMap<>(TipoMovimiento.class)));
    }

    public MovimientoStrategy obtener(TipoMovimiento tipo) {
        return Optional.ofNullable(estrategias.get(tipo))
                .orElseThrow(() -> new IllegalStateException("No hay estrategia para " + tipo));
    }
}
