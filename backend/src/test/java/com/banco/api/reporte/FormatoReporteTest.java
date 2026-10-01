package com.banco.api.reporte;

import com.banco.api.exception.SolicitudInvalidaException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FormatoReporteTest {

    @ParameterizedTest
    @CsvSource({"json, JSON", "JSON, JSON", "pdf, PDF", "Pdf, PDF", "' pdf ', PDF"})
    void reconoceElFormatoSinDistinguirMayusculas(String valor, FormatoReporte esperado) {
        assertThat(FormatoReporte.desde(valor)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"xml", "csv", ""})
    void rechazaFormatosNoSoportados(String valor) {
        assertThatThrownBy(() -> FormatoReporte.desde(valor))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("json, pdf");
    }
}
