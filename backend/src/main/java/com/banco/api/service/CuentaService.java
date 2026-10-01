package com.banco.api.service;

import com.banco.api.dto.CuentaPatchRequest;
import com.banco.api.dto.CuentaRequest;
import com.banco.api.dto.CuentaResponse;

import java.util.List;

public interface CuentaService {

    List<CuentaResponse> listar(String clienteId);

    CuentaResponse obtener(Long id);

    CuentaResponse crear(CuentaRequest request);

    CuentaResponse actualizar(Long id, CuentaRequest request);

    CuentaResponse actualizarParcial(Long id, CuentaPatchRequest request);

    void eliminar(Long id);
}
