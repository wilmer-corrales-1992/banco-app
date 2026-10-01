package com.banco.api.dto;

import com.banco.api.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RangoFechasTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 9, 1);
    private static final LocalDate FIN = LocalDate.of(2026, 9, 30);

    @Test
    void incluyeElDiaFinalCompleto() {
        RangoFechas rango = RangoFechas.de(List.of(INICIO, FIN));

        assertThat(rango.desde()).isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
        assertThat(rango.hasta()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
    }

    @Test
    void aceptaUnRangoDeUnSoloDia() {
        RangoFechas rango = RangoFechas.de(List.of(FIN, FIN));

        assertThat(rango.inicio()).isEqualTo(rango.fin());
    }

    @Test
    void rechazaFechasInvertidas() {
        assertThatThrownBy(() -> RangoFechas.de(List.of(FIN, INICIO)))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("posterior");
    }

    @Test
    void exigeExactamenteDosFechas() {
        assertThatThrownBy(() -> RangoFechas.de(List.of(INICIO)))
                .isInstanceOf(SolicitudInvalidaException.class);
        assertThatThrownBy(() -> RangoFechas.de(List.of(INICIO, FIN, FIN)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }
}
