package com.banco.api.controller;

import com.banco.api.dto.MovimientoRequest;
import com.banco.api.dto.MovimientoResponse;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.CupoDiarioExcedidoException;
import com.banco.api.exception.SaldoNoDisponibleException;
import com.banco.api.service.MovimientoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

    private static final String RETIRO = """
            {"numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": 575}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimientoService service;

    @Test
    void registrarDevuelve201ConElSaldoResultante() throws Exception {
        when(service.registrar(any(MovimientoRequest.class))).thenReturn(new MovimientoResponse(
                1L, LocalDateTime.of(2026, 9, 30, 10, 0), "478758", TipoCuenta.AHORRO,
                new BigDecimal("2000.00"), true, TipoMovimiento.DEBITO,
                new BigDecimal("-575.00"), new BigDecimal("1425.00"), "Jose Lema"));

        mockMvc.perform(post("/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/movimientos/1")))
                .andExpect(jsonPath("$.valor").value(-575.00))
                .andExpect(jsonPath("$.saldo").value(1425.00))
                .andExpect(jsonPath("$.fecha").value("2026-09-30T10:00:00"));
    }

    @Test
    void sinSaldoDevuelve422ConElMensajeExacto() throws Exception {
        when(service.registrar(any(MovimientoRequest.class))).thenThrow(new SaldoNoDisponibleException());

        mockMvc.perform(post("/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.estado").value(422))
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }

    @Test
    void cupoExcedidoDevuelve422ConElMensajeExacto() throws Exception {
        when(service.registrar(any(MovimientoRequest.class))).thenThrow(new CupoDiarioExcedidoException());

        mockMvc.perform(post("/movimientos").contentType(MediaType.APPLICATION_JSON).content(RETIRO))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("Cupo diario excedido"));
    }

    @Test
    void valorCeroONegativoSeRechazaAntesDeLlegarAlServicio() throws Exception {
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta": "478758", "tipoMovimiento": "DEBITO", "valor": -5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores[0].campo").value("valor"))
                .andExpect(jsonPath("$.errores[0].mensaje").value("El valor debe ser mayor que cero"));
        verify(service, never()).registrar(any());
    }

    @Test
    void tipoDeMovimientoDesconocidoDevuelve400() throws Exception {
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"numeroCuenta": "478758", "tipoMovimiento": "RETIRO", "valor": 5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje")
                        .value("Valor invalido para tipoMovimiento. Valores permitidos: CREDITO, DEBITO"));
    }

    @Test
    void cuerpoMalFormadoDevuelve400() throws Exception {
        mockMvc.perform(post("/movimientos").contentType(MediaType.APPLICATION_JSON).content("{malo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la solicitud no es valido"));
    }

    @Test
    void listarFiltraPorNumeroDeCuenta() throws Exception {
        when(service.listar("478758")).thenReturn(List.of());

        mockMvc.perform(get("/movimientos").param("numeroCuenta", "478758"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        verify(service).listar("478758");
    }
}
