package com.banco.api.reporte;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ReporteFormateadorResolver {

    private final Map<FormatoReporte, ReporteFormateador> formateadores;

    public ReporteFormateadorResolver(List<ReporteFormateador> formateadores) {
        this.formateadores = formateadores.stream()
                .collect(Collectors.toMap(
                        ReporteFormateador::formato,
                        Function.identity(),
                        (primero, segundo) -> {
                            throw new IllegalStateException("Formateador duplicado para " + primero.formato());
                        },
                        () -> new EnumMap<>(FormatoReporte.class)));
    }

    public ReporteFormateador obtener(FormatoReporte formato) {
        return Optional.ofNullable(formateadores.get(formato))
                .orElseThrow(() -> new IllegalStateException("No hay formateador para " + formato));
    }
}
