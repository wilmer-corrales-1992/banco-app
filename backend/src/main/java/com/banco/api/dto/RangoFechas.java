package com.banco.api.dto;

import com.banco.api.exception.SolicitudInvalidaException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Rango de fechas inclusivo en ambos extremos.
 */
public record RangoFechas(LocalDate inicio, LocalDate fin) {

    public RangoFechas {
        if (inicio.isAfter(fin)) {
            throw new SolicitudInvalidaException("La fecha inicial no puede ser posterior a la fecha final");
        }
    }

    public static RangoFechas de(List<LocalDate> fechas) {
        if (fechas == null || fechas.size() != 2) {
            throw new SolicitudInvalidaException(
                    "El parametro fecha debe tener el formato inicio,fin (por ejemplo 2026-09-01,2026-09-30)");
        }
        return new RangoFechas(fechas.get(0), fechas.get(1));
    }

    public LocalDateTime desde() {
        return inicio.atStartOfDay();
    }

    /**
     * Limite exclusivo: el inicio del dia siguiente a la fecha final.
     */
    public LocalDateTime hasta() {
        return fin.plusDays(1).atStartOfDay();
    }
}
