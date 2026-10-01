package com.banco.api.mapper;

import com.banco.api.dto.MovimientoResponse;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Movimiento;
import org.springframework.stereotype.Component;

/**
 * Los movimientos se construyen en el servicio porque su valor y saldo dependen de la estrategia.
 */
@Component
public class MovimientoMapper {

    public MovimientoResponse toResponse(Movimiento movimiento) {
        Cuenta cuenta = movimiento.getCuenta();
        return new MovimientoResponse(
                movimiento.getId(),
                movimiento.getFecha(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getEstado(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo(),
                cuenta.getCliente().getNombre());
    }
}
