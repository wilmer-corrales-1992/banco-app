package com.banco.api.service.impl;

import com.banco.api.dto.MovimientoPatchRequest;
import com.banco.api.dto.MovimientoRequest;
import com.banco.api.dto.MovimientoResponse;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Movimiento;
import com.banco.api.entity.TipoMovimiento;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.exception.ReglaNegocioException;
import com.banco.api.mapper.MovimientoMapper;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import com.banco.api.service.MovimientoService;
import com.banco.api.strategy.ContextoMovimiento;
import com.banco.api.strategy.MovimientoStrategy;
import com.banco.api.strategy.MovimientoStrategyResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cada movimiento guarda el saldo resultante, asi que los cambios se serializan por cuenta con un bloqueo
 * de fila y solo se permite modificar o eliminar el ultimo movimiento de cada cuenta.
 */
@Service
@Transactional(readOnly = true)
public class MovimientoServiceImpl implements MovimientoService {

    private static final int ESCALA_MONEDA = 2;

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoStrategyResolver estrategias;
    private final MovimientoMapper mapper;
    private final Clock clock;

    public MovimientoServiceImpl(MovimientoRepository movimientoRepository,
                                 CuentaRepository cuentaRepository,
                                 MovimientoStrategyResolver estrategias,
                                 MovimientoMapper mapper,
                                 Clock clock) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.estrategias = estrategias;
        this.mapper = mapper;
        this.clock = clock;
    }

    @Override
    public List<MovimientoResponse> listar(String numeroCuenta) {
        List<Movimiento> movimientos = Optional.ofNullable(numeroCuenta)
                .filter(valor -> !valor.isBlank())
                .map(valor -> movimientoRepository.findByCuenta_NumeroCuentaOrderByFechaDescIdDesc(valor.trim()))
                .orElseGet(movimientoRepository::findAllByOrderByFechaDescIdDesc);
        return movimientos.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public MovimientoResponse obtener(Long id) {
        return mapper.toResponse(movimientoRepository.findById(id)
                .orElseThrow(() -> noEncontrado(id)));
    }

    @Override
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        String numeroCuenta = request.numeroCuenta().trim();
        Cuenta cuenta = cuentaRepository.findByNumeroCuentaParaActualizar(numeroCuenta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con numero " + numeroCuenta));
        validarCuentaOperativa(cuenta);

        Movimiento movimiento = new Movimiento();
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(LocalDateTime.now(clock));
        BigDecimal saldoAnterior = movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(cuenta.getId())
                .map(Movimiento::getSaldo)
                .orElse(cuenta.getSaldoInicial());
        aplicar(movimiento, request.tipoMovimiento(), request.valor(), saldoAnterior);
        return mapper.toResponse(movimientoRepository.save(movimiento));
    }

    @Override
    @Transactional
    public MovimientoResponse actualizar(Long id, MovimientoRequest request) {
        Movimiento movimiento = bloquearUltimo(id);
        if (!movimiento.getCuenta().getNumeroCuenta().equals(request.numeroCuenta().trim())) {
            throw new OperacionNoPermitidaException("No se puede cambiar la cuenta de un movimiento");
        }
        return modificar(movimiento, request.tipoMovimiento(), request.valor());
    }

    @Override
    @Transactional
    public MovimientoResponse actualizarParcial(Long id, MovimientoPatchRequest request) {
        Movimiento movimiento = bloquearUltimo(id);
        TipoMovimiento tipo = Optional.ofNullable(request.tipoMovimiento())
                .orElse(movimiento.getTipoMovimiento());
        BigDecimal monto = Optional.ofNullable(request.valor())
                .orElseGet(() -> movimiento.getValor().abs());
        return modificar(movimiento, tipo, monto);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        movimientoRepository.delete(bloquearUltimo(id));
    }

    private MovimientoResponse modificar(Movimiento movimiento, TipoMovimiento tipo, BigDecimal monto) {
        validarCuentaOperativa(movimiento.getCuenta());
        BigDecimal saldoAnterior = movimiento.getSaldo().subtract(movimiento.getValor());
        aplicar(movimiento, tipo, monto, saldoAnterior);
        return mapper.toResponse(movimientoRepository.saveAndFlush(movimiento));
    }

    /**
     * Valida con la estrategia del tipo y deja en el movimiento el valor con signo y el saldo resultante.
     * Si el movimiento ya existia y era un debito, se descuenta de los debitos del dia para no contarlo dos veces.
     */
    private void aplicar(Movimiento movimiento, TipoMovimiento tipo, BigDecimal monto, BigDecimal saldoAnterior) {
        MovimientoStrategy estrategia = estrategias.obtener(tipo);
        estrategia.validar(new ContextoMovimiento(saldoAnterior, monto, debitosDelDia(movimiento)));
        BigDecimal valor = estrategia.valorConSigno(monto).setScale(ESCALA_MONEDA, RoundingMode.UNNECESSARY);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setValor(valor);
        movimiento.setSaldo(saldoAnterior.add(valor));
    }

    private BigDecimal debitosDelDia(Movimiento movimiento) {
        LocalDateTime desde = movimiento.getFecha().toLocalDate().atStartOfDay();
        BigDecimal total = movimientoRepository.totalDebitosEntre(
                movimiento.getCuenta().getId(), desde, desde.plusDays(1));
        BigDecimal propio = movimiento.getId() != null && movimiento.getTipoMovimiento() == TipoMovimiento.DEBITO
                ? movimiento.getValor().abs()
                : BigDecimal.ZERO;
        return total.subtract(propio);
    }

    /**
     * Bloquea el movimiento y luego su cuenta (mismo orden que el registro, que solo bloquea la cuenta),
     * y verifica que sea el ultimo de la cuenta.
     */
    private Movimiento bloquearUltimo(Long id) {
        Movimiento movimiento = movimientoRepository.findByIdParaActualizar(id)
                .orElseThrow(() -> noEncontrado(id));
        Long cuentaId = movimiento.getCuenta().getId();
        cuentaRepository.findByIdParaActualizar(cuentaId);
        movimientoRepository.findFirstByCuentaIdOrderByFechaDescIdDesc(cuentaId)
                .filter(ultimo -> ultimo.getId().equals(id))
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Solo se puede modificar o eliminar el ultimo movimiento de la cuenta"));
        return movimiento;
    }

    private void validarCuentaOperativa(Cuenta cuenta) {
        if (!Boolean.TRUE.equals(cuenta.getEstado())) {
            throw new ReglaNegocioException("La cuenta " + cuenta.getNumeroCuenta() + " esta inactiva");
        }
        if (!Boolean.TRUE.equals(cuenta.getCliente().getEstado())) {
            throw new ReglaNegocioException("El cliente " + cuenta.getCliente().getClienteId() + " esta inactivo");
        }
    }

    private RecursoNoEncontradoException noEncontrado(Long id) {
        return new RecursoNoEncontradoException("Movimiento no encontrado con id " + id);
    }
}
