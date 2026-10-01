package com.banco.api.service.impl;

import com.banco.api.dto.CuentaPatchRequest;
import com.banco.api.dto.CuentaRequest;
import com.banco.api.dto.CuentaResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.entity.Cuenta;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoDuplicadoException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.mapper.CuentaMapper;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.repository.MovimientoRepository;
import com.banco.api.service.CuentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final MovimientoRepository movimientoRepository;
    private final CuentaMapper mapper;

    public CuentaServiceImpl(CuentaRepository cuentaRepository,
                             ClienteRepository clienteRepository,
                             MovimientoRepository movimientoRepository,
                             CuentaMapper mapper) {
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.movimientoRepository = movimientoRepository;
        this.mapper = mapper;
    }

    @Override
    public List<CuentaResponse> listar(String clienteId) {
        List<Cuenta> cuentas = Optional.ofNullable(clienteId)
                .filter(valor -> !valor.isBlank())
                .map(valor -> cuentaRepository.findByCliente_ClienteId(valor.trim()))
                .orElseGet(cuentaRepository::findAllByOrderByNumeroCuentaAsc);
        return cuentas.stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public CuentaResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        validarNumeroUnico(request.numeroCuenta(), null);
        Cliente cliente = buscarCliente(request.clienteId());
        return mapper.toResponse(cuentaRepository.save(mapper.toEntity(request, cliente)));
    }

    @Override
    @Transactional
    public CuentaResponse actualizar(Long id, CuentaRequest request) {
        Cuenta cuenta = buscar(id);
        validarNumeroUnico(request.numeroCuenta(), id);
        validarCambioSaldoInicial(cuenta, request.saldoInicial());
        Cliente cliente = buscarCliente(request.clienteId());
        mapper.actualizar(cuenta, request, cliente);
        return mapper.toResponse(cuentaRepository.saveAndFlush(cuenta));
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(Long id, CuentaPatchRequest request) {
        Cuenta cuenta = buscar(id);
        validarNumeroUnico(request.numeroCuenta(), id);
        validarCambioSaldoInicial(cuenta, request.saldoInicial());
        Optional.ofNullable(request.clienteId())
                .map(this::buscarCliente)
                .ifPresent(cuenta::setCliente);
        mapper.actualizarParcial(cuenta, request);
        return mapper.toResponse(cuentaRepository.saveAndFlush(cuenta));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cuenta cuenta = buscar(id);
        if (movimientoRepository.existsByCuenta_Id(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar la cuenta " + cuenta.getNumeroCuenta() + " porque tiene movimientos");
        }
        cuentaRepository.delete(cuenta);
    }

    private Cuenta buscar(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cuenta no encontrada con id " + id));
    }

    private Cliente buscarCliente(String clienteId) {
        String codigo = clienteId.trim();
        return clienteRepository.findByClienteId(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con clienteId " + codigo));
    }

    private void validarNumeroUnico(String numeroCuenta, Long idActual) {
        Optional.ofNullable(numeroCuenta)
                .map(String::trim)
                .flatMap(cuentaRepository::findByNumeroCuenta)
                .filter(existente -> !existente.getId().equals(idActual))
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException("Ya existe la cuenta " + existente.getNumeroCuenta());
                });
    }

    /**
     * El saldo de cada movimiento se calcula desde el saldo inicial; cambiarlo despues romperia la historia.
     */
    private void validarCambioSaldoInicial(Cuenta cuenta, BigDecimal nuevoSaldo) {
        boolean cambia = nuevoSaldo != null && nuevoSaldo.compareTo(cuenta.getSaldoInicial()) != 0;
        if (cambia && movimientoRepository.existsByCuenta_Id(cuenta.getId())) {
            throw new OperacionNoPermitidaException(
                    "No se puede cambiar el saldo inicial de una cuenta que ya tiene movimientos");
        }
    }
}
