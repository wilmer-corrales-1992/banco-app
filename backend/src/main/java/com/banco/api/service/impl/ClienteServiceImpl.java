package com.banco.api.service.impl;

import com.banco.api.dto.ClientePatchRequest;
import com.banco.api.dto.ClienteRequest;
import com.banco.api.dto.ClienteResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.exception.OperacionNoPermitidaException;
import com.banco.api.exception.RecursoDuplicadoException;
import com.banco.api.exception.RecursoNoEncontradoException;
import com.banco.api.mapper.ClienteMapper;
import com.banco.api.repository.ClienteRepository;
import com.banco.api.repository.CuentaRepository;
import com.banco.api.service.ClienteService;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final ClienteMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              ClienteMapper mapper,
                              PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll(Sort.by("nombre", "id")).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public ClienteResponse obtener(Long id) {
        return mapper.toResponse(buscar(id));
    }

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarUnicidad(request.clienteId(), request.identificacion(), null);
        Cliente cliente = mapper.toEntity(request);
        cliente.setContrasena(passwordEncoder.encode(request.contrasena()));
        return mapper.toResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = buscar(id);
        validarUnicidad(request.clienteId(), request.identificacion(), id);
        mapper.actualizar(cliente, request);
        cambiarContrasena(cliente, request.contrasena());
        return mapper.toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(Long id, ClientePatchRequest request) {
        Cliente cliente = buscar(id);
        validarUnicidad(request.clienteId(), request.identificacion(), id);
        mapper.actualizarParcial(cliente, request);
        cambiarContrasena(cliente, request.contrasena());
        return mapper.toResponse(clienteRepository.saveAndFlush(cliente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Cliente cliente = buscar(id);
        if (cuentaRepository.existsByCliente_Id(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar el cliente " + cliente.getClienteId() + " porque tiene cuentas asociadas");
        }
        clienteRepository.delete(cliente);
    }

    private Cliente buscar(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id " + id));
    }

    private void validarUnicidad(String clienteId, String identificacion, Long idActual) {
        Optional.ofNullable(clienteId)
                .map(String::trim)
                .flatMap(clienteRepository::findByClienteId)
                .filter(existente -> !existente.getId().equals(idActual))
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException("Ya existe un cliente con clienteId " + existente.getClienteId());
                });
        Optional.ofNullable(identificacion)
                .map(String::trim)
                .flatMap(clienteRepository::findByIdentificacion)
                .filter(existente -> !existente.getId().equals(idActual))
                .ifPresent(existente -> {
                    throw new RecursoDuplicadoException(
                            "Ya existe una persona con identificacion " + existente.getIdentificacion());
                });
    }

    private void cambiarContrasena(Cliente cliente, String contrasena) {
        Optional.ofNullable(contrasena)
                .filter(valor -> !valor.isBlank())
                .map(passwordEncoder::encode)
                .ifPresent(cliente::setContrasena);
    }
}
