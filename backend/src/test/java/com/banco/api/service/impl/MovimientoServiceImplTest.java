package com.banco.api.service.impl;

import com.banco.api.DatosPrueba;
import com.banco.api.config.BancoProperties;
import com.banco.api.dto.MovimientoPatchRequest;
import com.banco.api.dto.MovimientoRequest;
import com.banco.api.dto.MovimientoResponse;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Movimiento;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.CupoDiarioExcedidoException;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.exception.ReglaNegocioException;
import com.banco.api.exception.SaldoNoDisponibleException;
import com.banco.api.mapper.MovimientoMapper;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import com.banco.api.strategy.CreditoStrategy;
import com.banco.api.strategy.DebitoStrategy;
import com.banco.api.strategy.MovimientoStrategyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 9, 30, 10, 0);
    private static final LocalDateTime INICIO_DIA = LocalDateTime.of(2026, 9, 30, 0, 0);
    private static final LocalDateTime INICIO_DIA_SIGUIENTE = LocalDateTime.of(2026, 10, 1, 0, 0);

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    private MovimientoServiceImpl service;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        MovimientoStrategyResolver estrategias = new MovimientoStrategyResolver(List.of(
                new CreditoStrategy(),
                new DebitoStrategy(new BancoProperties(new BigDecimal("1000")))));
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new MovimientoServiceImpl(
                movimientoRepository, cuentaRepository, estrategias, new MovimientoMapper(), reloj);

        cuenta = DatosPrueba.cuenta(2L, "478758", TipoCuenta.AHORRO, "2000.00", DatosPrueba.joseLema());
        lenient().when(movimientoRepository.save(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(movimientoRepository.saveAndFlush(any(Movimiento.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(movimientoRepository.totalDebitosEntre(anyLong(), any(), any())).thenReturn(BigDecimal.ZERO);
    }

    @Test
    void registrarDebitoSinMovimientosPreviosParteDelSaldoInicial() {
        cuentaDisponible();
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.empty());

        MovimientoResponse respuesta = service.registrar(debito("575"));

        assertThat(respuesta.tipoMovimiento()).isEqualTo(TipoMovimiento.DEBITO);
        assertThat(respuesta.valor()).isEqualByComparingTo("-575.00");
        assertThat(respuesta.saldo()).isEqualByComparingTo("1425.00");
        assertThat(respuesta.fecha()).isEqualTo(AHORA);
        assertThat(respuesta.cliente()).isEqualTo("Jose Lema");
        verify(movimientoRepository).totalDebitosEntre(2L, INICIO_DIA, INICIO_DIA_SIGUIENTE);
    }

    @Test
    void registrarCreditoSumaSobreElSaldoDelUltimoMovimiento() {
        cuentaDisponible();
        Movimiento ultimo = DatosPrueba.movimiento(
                7L, cuenta, AHORA.minusDays(1), TipoMovimiento.DEBITO, "-575.00", "1425.00");
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.of(ultimo));

        MovimientoResponse respuesta = service.registrar(
                new MovimientoRequest("478758", TipoMovimiento.CREDITO, new BigDecimal("600")));

        assertThat(respuesta.valor()).isEqualByComparingTo("600.00");
        assertThat(respuesta.saldo()).isEqualByComparingTo("2025.00");
        assertThat(respuesta.valor().scale()).isEqualTo(2);
    }

    @Test
    void registrarDebitoConSaldoCeroLanzaSaldoNoDisponibleYNoGuarda() {
        cuentaDisponible();
        Movimiento ultimo = DatosPrueba.movimiento(
                7L, cuenta, AHORA.minusDays(1), TipoMovimiento.DEBITO, "-2000.00", "0.00");
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.of(ultimo));

        assertThatThrownBy(() -> service.registrar(debito("1")))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarDebitoQueSuperaElCupoDelDiaLanzaCupoDiarioExcedido() {
        cuentaDisponible();
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.empty());
        when(movimientoRepository.totalDebitosEntre(2L, INICIO_DIA, INICIO_DIA_SIGUIENTE))
                .thenReturn(new BigDecimal("600.00"));

        assertThatThrownBy(() -> service.registrar(debito("500")))
                .isInstanceOf(CupoDiarioExcedidoException.class)
                .hasMessage("Cupo diario excedido");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void registrarEnCuentaInactivaLanzaReglaDeNegocio() {
        cuenta.setEstado(false);
        cuentaDisponible();

        assertThatThrownBy(() -> service.registrar(debito("10")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void registrarConClienteInactivoLanzaReglaDeNegocio() {
        cuenta.getCliente().setEstado(false);
        cuentaDisponible();

        assertThatThrownBy(() -> service.registrar(debito("10")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("inactivo");
    }

    @Test
    void registrarEnCuentaInexistenteLanzaNoEncontrado() {
        when(cuentaRepository.findByNumeroCuentaParaActualizar("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(
                new MovimientoRequest("999", TipoMovimiento.CREDITO, BigDecimal.TEN)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void soloSePuedeModificarElUltimoMovimientoDeLaCuenta() {
        Movimiento antiguo = DatosPrueba.movimiento(
                5L, cuenta, AHORA.minusDays(2), TipoMovimiento.CREDITO, "100.00", "2100.00");
        Movimiento ultimo = DatosPrueba.movimiento(
                6L, cuenta, AHORA.minusDays(1), TipoMovimiento.CREDITO, "100.00", "2200.00");
        when(movimientoRepository.findByIdParaActualizar(5L)).thenReturn(Optional.of(antiguo));
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.of(ultimo));

        assertThatThrownBy(() -> service.actualizarParcial(5L, new MovimientoPatchRequest(null, BigDecimal.ONE)))
                .isInstanceOf(OperacionNoPermitidaException.class);
        assertThatThrownBy(() -> service.eliminar(5L))
                .isInstanceOf(OperacionNoPermitidaException.class);
        verify(movimientoRepository, never()).delete(any());
    }

    @Test
    void modificarUnDebitoNoCuentaSuPropioValorEnElCupoDelDia() {
        Movimiento ultimo = DatosPrueba.movimiento(
                6L, cuenta, AHORA, TipoMovimiento.DEBITO, "-600.00", "1400.00");
        ultimoMovimiento(ultimo);
        when(movimientoRepository.totalDebitosEntre(2L, INICIO_DIA, INICIO_DIA_SIGUIENTE))
                .thenReturn(new BigDecimal("600.00"));

        MovimientoResponse respuesta = service.actualizarParcial(
                6L, new MovimientoPatchRequest(null, new BigDecimal("900")));

        assertThat(respuesta.valor()).isEqualByComparingTo("-900.00");
        assertThat(respuesta.saldo()).isEqualByComparingTo("1100.00");
    }

    @Test
    void cambiarUnDebitoACreditoRecalculaElSaldoDesdeElSaldoAnterior() {
        Movimiento ultimo = DatosPrueba.movimiento(
                6L, cuenta, AHORA, TipoMovimiento.DEBITO, "-575.00", "1425.00");
        ultimoMovimiento(ultimo);

        MovimientoResponse respuesta = service.actualizar(
                6L, new MovimientoRequest("478758", TipoMovimiento.CREDITO, new BigDecimal("50")));

        assertThat(respuesta.tipoMovimiento()).isEqualTo(TipoMovimiento.CREDITO);
        assertThat(respuesta.saldo()).isEqualByComparingTo("2050.00");
    }

    @Test
    void noSePuedeMoverUnMovimientoAOtraCuenta() {
        Movimiento ultimo = DatosPrueba.movimiento(
                6L, cuenta, AHORA, TipoMovimiento.CREDITO, "50.00", "2050.00");
        ultimoMovimiento(ultimo);

        assertThatThrownBy(() -> service.actualizar(
                6L, new MovimientoRequest("585545", TipoMovimiento.CREDITO, BigDecimal.ONE)))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageContaining("cuenta");
    }

    @Test
    void eliminarElUltimoMovimientoLoBorra() {
        Movimiento ultimo = DatosPrueba.movimiento(
                6L, cuenta, AHORA, TipoMovimiento.CREDITO, "50.00", "2050.00");
        ultimoMovimiento(ultimo);

        service.eliminar(6L);

        ArgumentCaptor<Movimiento> borrado = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).delete(borrado.capture());
        assertThat(borrado.getValue().getId()).isEqualTo(6L);
    }

    private void cuentaDisponible() {
        when(cuentaRepository.findByNumeroCuentaParaActualizar("478758")).thenReturn(Optional.of(cuenta));
    }

    private void ultimoMovimiento(Movimiento ultimo) {
        when(movimientoRepository.findByIdParaActualizar(ultimo.getId())).thenReturn(Optional.of(ultimo));
        when(cuentaRepository.findByIdParaActualizar(eq(2L))).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(2L)).thenReturn(Optional.of(ultimo));
    }

    private static MovimientoRequest debito(String valor) {
        return new MovimientoRequest("478758", TipoMovimiento.DEBITO, new BigDecimal(valor));
    }
}
