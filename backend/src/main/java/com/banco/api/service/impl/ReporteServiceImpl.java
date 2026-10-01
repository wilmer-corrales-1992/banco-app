package com.banco.api.service.impl;

import com.banco.api.dto.RangoFechas;
import com.banco.api.dto.ReporteCuenta;
import com.banco.api.dto.ReporteMovimiento;
import com.banco.api.dto.ReporteResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Movimiento;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import com.banco.api.service.ReporteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private static final BigDecimal CERO = BigDecimal.ZERO.setScale(2);

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final Clock clock;

    public ReporteServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              MovimientoRepository movimientoRepository,
                              Clock clock) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.clock = clock;
    }

    @Override
    public ReporteResponse generar(String clienteId, RangoFechas rango) {
        String codigo = clienteId.trim();
        Cliente cliente = clienteRepository.findByClienteId(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con clienteId " + codigo));

        Map<Long, List<Movimiento>> movimientosPorCuenta = movimientoRepository
                .findDelClienteEntre(codigo, rango.desde(), rango.hasta()).stream()
                .collect(Collectors.groupingBy(movimiento -> movimiento.getCuenta().getId()));

        List<ReporteCuenta> cuentas = cuentaRepository.findByCliente_ClienteId(codigo).stream()
                .sorted(Comparator.comparing(Cuenta::getNumeroCuenta))
                .map(cuenta -> resumirCuenta(
                        cuenta, movimientosPorCuenta.getOrDefault(cuenta.getId(), List.of()), rango))
                .toList();

        return new ReporteResponse(
                cliente.getClienteId(),
                cliente.getNombre(),
                rango.inicio(),
                rango.fin(),
                LocalDateTime.now(clock),
                sumar(cuentas, ReporteCuenta::totalCreditos),
                sumar(cuentas, ReporteCuenta::totalDebitos),
                cuentas);
    }

    private ReporteCuenta resumirCuenta(Cuenta cuenta, List<Movimiento> movimientos, RangoFechas rango) {
        List<ReporteMovimiento> filas = movimientos.stream()
                .map(movimiento -> aFila(cuenta, movimiento))
                .toList();
        BigDecimal saldoDisponible = movimientoRepository
                .findFirstByCuentaIdAndFechaLessThanOrderByFechaDescIdDesc(cuenta.getId(), rango.hasta())
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
        return new ReporteCuenta(
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getEstado(),
                saldoDisponible,
                totalPorTipo(movimientos, TipoMovimiento.CREDITO),
                totalPorTipo(movimientos, TipoMovimiento.DEBITO),
                filas);
    }

    private static ReporteMovimiento aFila(Cuenta cuenta, Movimiento movimiento) {
        return new ReporteMovimiento(
                movimiento.getFecha(),
                cuenta.getCliente().getNombre(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                movimiento.getSaldo().subtract(movimiento.getValor()),
                cuenta.getEstado(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo());
    }

    private static BigDecimal totalPorTipo(List<Movimiento> movimientos, TipoMovimiento tipo) {
        return movimientos.stream()
                .filter(movimiento -> movimiento.getTipoMovimiento() == tipo)
                .map(movimiento -> movimiento.getValor().abs())
                .reduce(CERO, BigDecimal::add);
    }

    private static BigDecimal sumar(List<ReporteCuenta> cuentas, Function<ReporteCuenta, BigDecimal> total) {
        return cuentas.stream()
                .map(total)
                .reduce(CERO, BigDecimal::add);
    }
}
