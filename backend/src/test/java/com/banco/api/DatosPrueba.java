package com.banco.api;

import com.banco.api.entity.Cliente;
import com.banco.api.entity.Cuenta;
import com.banco.api.entity.Genero;
import com.banco.api.entity.Movimiento;
import com.banco.api.entity.TipoCuenta;
import com.banco.api.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidades de ejemplo basadas en los datos del enunciado.
 */
public final class DatosPrueba {

    private DatosPrueba() {
    }

    public static Cliente joseLema() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setClienteId("jlema");
        cliente.setNombre("Jose Lema");
        cliente.setGenero(Genero.MASCULINO);
        cliente.setEdad(30);
        cliente.setIdentificacion("1700000001");
        cliente.setDireccion("Otavalo sn y principal");
        cliente.setTelefono("098254785");
        cliente.setContrasena("$2a$10$hash");
        cliente.setEstado(true);
        return cliente;
    }

    public static Cuenta cuenta(Long id, String numero, TipoCuenta tipo, String saldoInicial, Cliente cliente) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(id);
        cuenta.setNumeroCuenta(numero);
        cuenta.setTipoCuenta(tipo);
        cuenta.setSaldoInicial(new BigDecimal(saldoInicial));
        cuenta.setEstado(true);
        cuenta.setCliente(cliente);
        return cuenta;
    }

    public static Movimiento movimiento(Long id, Cuenta cuenta, LocalDateTime fecha,
                                        TipoMovimiento tipo, String valor, String saldo) {
        Movimiento movimiento = new Movimiento();
        movimiento.setId(id);
        movimiento.setCuenta(cuenta);
        movimiento.setFecha(fecha);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setValor(new BigDecimal(valor));
        movimiento.setSaldo(new BigDecimal(saldo));
        return movimiento;
    }
}
