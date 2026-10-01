package com.banco.api.service;

import com.banco.api.dto.MovimientoPatchRequest;
import com.banco.api.dto.MovimientoRequest;
import com.banco.api.dto.MovimientoResponse;

import java.util.List;

public interface MovimientoService {

    List<MovimientoResponse> listar(String numeroCuenta);

    MovimientoResponse obtener(Long id);

    MovimientoResponse registrar(MovimientoRequest request);

    MovimientoResponse actualizar(Long id, MovimientoRequest request);

    MovimientoResponse actualizarParcial(Long id, MovimientoPatchRequest request);

    void eliminar(Long id);
}
