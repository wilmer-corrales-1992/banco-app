package com.banco.api.controller;

import com.banco.api.dto.RangoFechas;
import com.banco.api.dto.ReporteCuenta;
import com.banco.api.dto.ReporteMovimiento;
import com.banco.api.dto.ReporteResponse;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.reporte.JsonReporteFormateador;
import com.banco.api.reporte.PdfReporteFormateador;
import com.banco.api.reporte.ReporteFormateadorResolver;
import com.banco.api.service.ReporteService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReporteController.class)
@Import({ReporteFormateadorResolver.class, JsonReporteFormateador.class, PdfReporteFormateador.class})
class ReporteControllerTest {

    private static final RangoFechas SEPTIEMBRE = new RangoFechas(
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService service;

    @Test
    void sinFormatoDevuelveElReporteEnJson() throws Exception {
        when(service.generar("jlema", SEPTIEMBRE)).thenReturn(reporte());

        mockMvc.perform(get("/reportes")
                        .param("fecha", "2026-09-01,2026-09-30")
                        .param("clienteId", "jlema"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Jose Lema"))
                .andExpect(jsonPath("$.fechaInicio").value("2026-09-01"))
                .andExpect(jsonPath("$.totalCreditos").value(100.00))
                .andExpect(jsonPath("$.totalDebitos").value(575.00))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].movimiento").value(-575.00))
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].saldoDisponible").value(1425.00));
    }

    @Test
    void formatoPdfDevuelveUnPdfValidoEnBase64() throws Exception {
        when(service.generar("jlema", SEPTIEMBRE)).thenReturn(reporte());

        String cuerpo = mockMvc.perform(get("/reportes")
                        .param("fecha", "2026-09-01,2026-09-30")
                        .param("clienteId", "jlema")
                        .param("formato", "pdf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreArchivo").value("estado-cuenta-jlema-20260901-20260930.pdf"))
                .andExpect(jsonPath("$.tipoContenido").value("application/pdf"))
                .andReturn().getResponse().getContentAsString();

        byte[] pdf = Base64.getDecoder().decode(JsonPath.<String>read(cuerpo, "$.contenidoBase64"));
        assertThat(new String(pdf, 0, 5, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF-");
        assertThat(pdf.length).isGreaterThan(1000);
    }

    @Test
    void aceptaLasFechasComoDosParametrosSeparados() throws Exception {
        when(service.generar("jlema", SEPTIEMBRE)).thenReturn(reporte());

        mockMvc.perform(get("/reportes")
                        .param("fecha", "2026-09-01", "2026-09-30")
                        .param("clienteId", "jlema"))
                .andExpect(status().isOk());
    }

    @Test
    void fechasInvertidasDevuelven400() throws Exception {
        mockMvc.perform(get("/reportes")
                        .param("fecha", "2026-09-30,2026-09-01")
                        .param("clienteId", "jlema"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("La fecha inicial no puede ser posterior a la fecha final"));
        verify(service, never()).generar(any(), any());
    }

    @Test
    void fechaConFormatoIncorrectoIndicaElFormatoEsperado() throws Exception {
        mockMvc.perform(get("/reportes")
                        .param("fecha", "01/09/2026,30/09/2026")
                        .param("clienteId", "jlema"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("El parametro fecha tiene un valor invalido (formato esperado yyyy-MM-dd)"));
    }

    @Test
    void formatoNoSoportadoDevuelve400() throws Exception {
        mockMvc.perform(get("/reportes")
                        .param("fecha", "2026-09-01,2026-09-30")
                        .param("clienteId", "jlema")
                        .param("formato", "xml"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Formato de reporte no soportado: xml. Valores permitidos: json, pdf"));
        verify(service, never()).generar(eq("jlema"), any());
    }

    @Test
    void sinClienteIdDevuelve400() throws Exception {
        mockMvc.perform(get("/reportes").param("fecha", "2026-09-01,2026-09-30"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El parametro clienteId es obligatorio"));
    }

    private static ReporteResponse reporte() {
        ReporteMovimiento retiro = new ReporteMovimiento(
                LocalDateTime.of(2026, 9, 10, 9, 30), "Jose Lema", "478758", TipoCuenta.AHORRO,
                new BigDecimal("2000.00"), true, TipoMovimiento.DEBITO,
                new BigDecimal("-575.00"), new BigDecimal("1425.00"));
        ReporteMovimiento deposito = new ReporteMovimiento(
                LocalDateTime.of(2026, 9, 20, 16, 45), "Jose Lema", "478758", TipoCuenta.AHORRO,
                new BigDecimal("1425.00"), true, TipoMovimiento.CREDITO,
                new BigDecimal("100.00"), new BigDecimal("1525.00"));
        ReporteCuenta ahorro = new ReporteCuenta(
                "478758", TipoCuenta.AHORRO, new BigDecimal("2000.00"), true, new BigDecimal("1525.00"),
                new BigDecimal("100.00"), new BigDecimal("575.00"), List.of(retiro, deposito));
        ReporteCuenta corriente = new ReporteCuenta(
                "585545", TipoCuenta.CORRIENTE, new BigDecimal("1000.00"), true, new BigDecimal("1000.00"),
                new BigDecimal("0.00"), new BigDecimal("0.00"), List.of());
        return new ReporteResponse(
                "jlema", "Jose Lema", SEPTIEMBRE.inicio(), SEPTIEMBRE.fin(),
                LocalDateTime.of(2026, 9, 30, 12, 0),
                new BigDecimal("100.00"), new BigDecimal("575.00"), List.of(ahorro, corriente));
    }
}
