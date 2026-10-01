package com.banco.api.mapper;

import com.banco.api.dto.CuentaPatchRequest;
import com.banco.api.dto.CuentaRequest;
import com.banco.api.dto.CuentaResponse;
import com.banco.api.entity.Cliente;
import com.banco.api.entity.Cuenta;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.banco.api.mapper.ActualizacionParcial.siPresente;

/**
 * El cliente lo resuelve el servicio a partir del clienteId de negocio.
 */
@Component
public class CuentaMapper {

    public Cuenta toEntity(CuentaRequest request, Cliente cliente) {
        Cuenta cuenta = new Cuenta();
        actualizar(cuenta, request, cliente);
        return cuenta;
    }

    public void actualizar(Cuenta cuenta, CuentaRequest request, Cliente cliente) {
        cuenta.setNumeroCuenta(request.numeroCuenta().trim());
        cuenta.setTipoCuenta(request.tipoCuenta());
        cuenta.setSaldoInicial(aEscalaMoneda(request.saldoInicial()));
        siPresente(request.estado(), cuenta::setEstado);
        cuenta.setCliente(cliente);
    }

    public void actualizarParcial(Cuenta cuenta, CuentaPatchRequest request) {
        siPresente(request.numeroCuenta(), valor -> cuenta.setNumeroCuenta(valor.trim()));
        siPresente(request.tipoCuenta(), cuenta::setTipoCuenta);
        siPresente(request.saldoInicial(), valor -> cuenta.setSaldoInicial(aEscalaMoneda(valor)));
        siPresente(request.estado(), cuenta::setEstado);
    }

    public CuentaResponse toResponse(Cuenta cuenta) {
        return new CuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getTipoCuenta(),
                cuenta.getSaldoInicial(),
                cuenta.getEstado(),
                cuenta.getCliente().getClienteId(),
                cuenta.getCliente().getNombre());
    }

    private static BigDecimal aEscalaMoneda(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }
}
