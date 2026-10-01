package com.banco.api.service;

import com.banco.api.dto.ClientePatchRequest;
import com.banco.api.dto.ClienteRequest;
import com.banco.api.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {

    List<ClienteResponse> listar();

    ClienteResponse obtener(Long id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Long id, ClienteRequest request);

    ClienteResponse actualizarParcial(Long id, ClientePatchRequest request);

    void eliminar(Long id);
}
