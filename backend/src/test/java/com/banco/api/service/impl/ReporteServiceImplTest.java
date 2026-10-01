package com.banco.api.service.impl;

import com.banco.api.DatosPrueba;
import com.banco.api.dto.RangoFechas;
import com.banco.api.dto.ReporteCuenta;
import com.banco.api.dto.ReporteMovimiento;
import com.banco.api.dto.ReporteResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Movimiento;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    private static final RangoFechas SEPTIEMBRE = new RangoFechas(
            LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private MovimientoRepository movimientoRepository;

    private ReporteServiceImpl service;
    private Cliente jose;
    private Cuenta ahorro;
    private Cuenta corriente;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 9, 30, 12, 0).toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        service = new ReporteServiceImpl(clienteRepository, cuentaRepository, movimientoRepository, reloj);
        jose = DatosPrueba.joseLema();
        ahorro = DatosPrueba.cuenta(2L, "478758", TipoCuenta.AHORRO, "2000.00", jose);
        corriente = DatosPrueba.cuenta(1L, "585545", TipoCuenta.CORRIENTE, "1000.00", jose);
    }

    @Test
    void armaElEstadoDeCuentaConTotalesPorCuentaYGenerales() {
        Movimiento retiro = DatosPrueba.movimiento(1L, ahorro, LocalDateTime.of(2026, 9, 10, 9, 30),
                TipoMovimiento.DEBITO, "-575.00", "1425.00");
        Movimiento deposito = DatosPrueba.movimiento(2L, ahorro, LocalDateTime.of(2026, 9, 20, 16, 45),
                TipoMovimiento.CREDITO, "100.00", "1525.00");
        Movimiento deAgosto = DatosPrueba.movimiento(3L, corriente, LocalDateTime.of(2026, 8, 15, 10, 0),
                TipoMovimiento.CREDITO, "200.00", "1200.00");

        when(clienteRepository.findByClienteId("jlema")).thenReturn(Optional.of(jose));
        when(movimientoRepository.findDelClienteEntre("jlema", SEPTIEMBRE.desde(), SEPTIEMBRE.hasta()))
                .thenReturn(List.of(retiro, deposito));
        when(cuentaRepository.findByCliente_ClienteId("jlema")).thenReturn(List.of(ahorro, corriente));
        when(movimientoRepository.findFirstByCuentaIdAndFechaLessThanOrderByFechaDescIdDesc(2L, SEPTIEMBRE.hasta()))
                .thenReturn(Optional.of(deposito));
        when(movimientoRepository.findFirstByCuentaIdAndFechaLessThanOrderByFechaDescIdDesc(1L, SEPTIEMBRE.hasta()))
                .thenReturn(Optional.of(deAgosto));

        ReporteResponse reporte = service.generar(" jlema ", SEPTIEMBRE);

        assertThat(reporte.cliente()).isEqualTo("Jose Lema");
        assertThat(reporte.totalCreditos()).isEqualByComparingTo("100.00");
        assertThat(reporte.totalDebitos()).isEqualByComparingTo("575.00");
        assertThat(reporte.fechaGeneracion()).isEqualTo(LocalDateTime.of(2026, 9, 30, 12, 0));
        assertThat(reporte.cuentas()).extracting(ReporteCuenta::numeroCuenta).containsExactly("478758", "585545");

        ReporteCuenta cuentaAhorro = reporte.cuentas().get(0);
        assertThat(cuentaAhorro.saldoDisponible()).isEqualByComparingTo("1525.00");
        assertThat(cuentaAhorro.totalCreditos()).isEqualByComparingTo("100.00");
        assertThat(cuentaAhorro.totalDebitos()).isEqualByComparingTo("575.00");
        assertThat(cuentaAhorro.movimientos())
                .extracting(ReporteMovimiento::saldoInicial, ReporteMovimiento::movimiento,
                        ReporteMovimiento::saldoDisponible)
                .containsExactly(
                        tuple(new BigDecimal("2000.00"), new BigDecimal("-575.00"), new BigDecimal("1425.00")),
                        tuple(new BigDecimal("1425.00"), new BigDecimal("100.00"), new BigDecimal("1525.00")));

        ReporteCuenta cuentaCorriente = reporte.cuentas().get(1);
        assertThat(cuentaCorriente.movimientos()).isEmpty();
        assertThat(cuentaCorriente.saldoDisponible()).isEqualByComparingTo("1200.00");
        assertThat(cuentaCorriente.totalCreditos()).isEqualByComparingTo("0.00");
        assertThat(cuentaCorriente.totalDebitos().scale()).isEqualTo(2);
    }

    @Test
    void sinMovimientosNiAnterioresElSaldoDisponibleEsElSaldoInicial() {
        when(clienteRepository.findByClienteId("jlema")).thenReturn(Optional.of(jose));
        when(movimientoRepository.findDelClienteEntre("jlema", SEPTIEMBRE.desde(), SEPTIEMBRE.hasta()))
                .thenReturn(List.of());
        when(cuentaRepository.findByCliente_ClienteId("jlema")).thenReturn(List.of(corriente));
        when(movimientoRepository.findFirstByCuentaIdAndFechaLessThanOrderByFechaDescIdDesc(1L, SEPTIEMBRE.hasta()))
                .thenReturn(Optional.empty());

        ReporteResponse reporte = service.generar("jlema", SEPTIEMBRE);

        assertThat(reporte.cuentas().get(0).saldoDisponible()).isEqualByComparingTo("1000.00");
        assertThat(reporte.totalCreditos()).isEqualByComparingTo("0.00");
    }

    @Test
    void conClienteInexistenteLanzaNoEncontrado() {
        when(clienteRepository.findByClienteId("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generar("nadie", SEPTIEMBRE))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
