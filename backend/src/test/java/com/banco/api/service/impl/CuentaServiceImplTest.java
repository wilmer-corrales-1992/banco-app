package com.banco.api.service.impl;

import com.banco.api.DatosPrueba;
import com.banco.api.dto.CuentaPatchRequest;
import com.banco.api.dto.CuentaRequest;
import com.banco.api.dto.CuentaResponse;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoDuplicadoException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.mapper.CuentaMapper;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    private CuentaServiceImpl service;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        service = new CuentaServiceImpl(cuentaRepository, clienteRepository, movimientoRepository, new CuentaMapper());
        cuenta = DatosPrueba.cuenta(2L, "478758", TipoCuenta.AHORRO, "2000.00", DatosPrueba.joseLema());
    }

    @Test
    void crearAsociaLaCuentaAlClientePorSuClienteId() {
        when(cuentaRepository.findByNumeroCuenta("585545")).thenReturn(Optional.empty());
        when(clienteRepository.findByClienteId("jlema")).thenReturn(Optional.of(DatosPrueba.joseLema()));
        when(cuentaRepository.save(any(Cuenta.class))).thenAnswer(inv -> inv.getArgument(0));

        CuentaResponse respuesta = service.crear(new CuentaRequest(
                "585545", TipoCuenta.CORRIENTE, new BigDecimal("1000"), null, "jlema"));

        assertThat(respuesta.cliente()).isEqualTo("Jose Lema");
        assertThat(respuesta.saldoInicial()).isEqualByComparingTo("1000.00");
        assertThat(respuesta.saldoInicial().scale()).isEqualTo(2);
        assertThat(respuesta.estado()).isTrue();
    }

    @Test
    void crearConClienteInexistenteLanzaNoEncontrado() {
        when(cuentaRepository.findByNumeroCuenta("111")).thenReturn(Optional.empty());
        when(clienteRepository.findByClienteId("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new CuentaRequest(
                "111", TipoCuenta.AHORRO, BigDecimal.ONE, null, "nadie")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("nadie");
    }

    @Test
    void crearConNumeroRepetidoLanzaDuplicado() {
        when(cuentaRepository.findByNumeroCuenta("478758")).thenReturn(Optional.of(cuenta));

        assertThatThrownBy(() -> service.crear(new CuentaRequest(
                "478758", TipoCuenta.AHORRO, BigDecimal.ONE, null, "jlema")))
                .isInstanceOf(RecursoDuplicadoException.class);
    }

    @Test
    void noSeCambiaElSaldoInicialSiLaCuentaTieneMovimientos() {
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.existsByCuenta_Id(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.actualizarParcial(2L, new CuentaPatchRequest(
                null, null, new BigDecimal("900"), null, null)))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("saldo inicial");
    }

    @Test
    void enviarElMismoSaldoInicialNoSeConsideraUnCambio() {
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.saveAndFlush(cuenta)).thenReturn(cuenta);

        CuentaResponse respuesta = service.actualizarParcial(2L, new CuentaPatchRequest(
                null, null, new BigDecimal("2000"), false, null));

        assertThat(respuesta.estado()).isFalse();
        verify(movimientoRepository, never()).existsByCuenta_Id(any());
    }

    @Test
    void noSeEliminaUnaCuentaConMovimientos() {
        when(cuentaRepository.findById(2L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.existsByCuenta_Id(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.eliminar(2L)).isInstanceOf(OperacionNoPermitidaException.class);
        verify(cuentaRepository, never()).delete(any());
    }
}
