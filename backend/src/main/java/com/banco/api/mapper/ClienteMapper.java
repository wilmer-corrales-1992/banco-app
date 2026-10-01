package com.banco.api.mapper;

import com.banco.api.dto.ClientePatchRequest;
import com.banco.api.dto.ClienteRequest;
import com.banco.api.dto.ClienteResponse;
import com.banco.api.entity.Cliente;
import org.springframework.stereotype.Component;

import static com.banco.api.mapper.ActualizacionParcial.siPresente;

/**
 * La contrasena no se mapea aqui: el servicio la cifra antes de guardarla.
 */
@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteRequest request) {
        Cliente cliente = new Cliente();
        actualizar(cliente, request);
        return cliente;
    }

    public void actualizar(Cliente cliente, ClienteRequest request) {
        cliente.setNombre(request.nombre().trim());
        cliente.setGenero(request.genero());
        cliente.setEdad(request.edad());
        cliente.setIdentificacion(request.identificacion().trim());
        cliente.setDireccion(request.direccion().trim());
        cliente.setTelefono(request.telefono().trim());
        cliente.setClienteId(request.clienteId().trim());
        siPresente(request.estado(), cliente::setEstado);
    }

    public void actualizarParcial(Cliente cliente, ClientePatchRequest request) {
        siPresente(request.nombre(), valor -> cliente.setNombre(valor.trim()));
        siPresente(request.genero(), cliente::setGenero);
        siPresente(request.edad(), cliente::setEdad);
        siPresente(request.identificacion(), valor -> cliente.setIdentificacion(valor.trim()));
        siPresente(request.direccion(), valor -> cliente.setDireccion(valor.trim()));
        siPresente(request.telefono(), valor -> cliente.setTelefono(valor.trim()));
        siPresente(request.clienteId(), valor -> cliente.setClienteId(valor.trim()));
        siPresente(request.estado(), cliente::setEstado);
    }

    public ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getClienteId(),
                cliente.getNombre(),
                cliente.getGenero(),
                cliente.getEdad(),
                cliente.getIdentificacion(),
                cliente.getDireccion(),
                cliente.getTelefono(),
                cliente.getEstado());
    }
}
