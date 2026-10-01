import { Cliente } from '../app/core/models/cliente';
import { Cuenta } from '../app/core/models/cuenta';
import { Movimiento } from '../app/core/models/movimiento';
import { Reporte } from '../app/core/models/reporte';

/** Datos del enunciado para las pruebas. */

export const JOSE: Cliente = {
  id: 1,
  clienteId: 'jlema',
  nombre: 'José Lema',
  genero: 'MASCULINO',
  edad: 30,
  identificacion: '1700000001',
  direccion: 'Otavalo sn y principal',
  telefono: '098254785',
  estado: true,
};

export const MARIANELA: Cliente = {
  id: 2,
  clienteId: 'mmontalvo',
  nombre: 'Marianela Montalvo',
  genero: 'FEMENINO',
  edad: 28,
  identificacion: '1700000002',
  direccion: 'Amazonas y NNUU',
  telefono: '097548965',
  estado: true,
};

export const CUENTA_AHORRO: Cuenta = {
  id: 2,
  numeroCuenta: '478758',
  tipoCuenta: 'AHORRO',
  saldoInicial: 2000,
  estado: true,
  clienteId: 'jlema',
  cliente: 'José Lema',
};

export const CUENTA_CERO: Cuenta = {
  id: 4,
  numeroCuenta: '496825',
  tipoCuenta: 'AHORRO',
  saldoInicial: 540,
  estado: true,
  clienteId: 'mmontalvo',
  cliente: 'Marianela Montalvo',
};

export const RETIRO: Movimiento = {
  id: 1,
  fecha: '2026-10-01T10:16:00',
  numeroCuenta: '478758',
  tipoCuenta: 'AHORRO',
  saldoInicial: 2000,
  estado: true,
  tipoMovimiento: 'DEBITO',
  valor: -575,
  saldo: 1425,
  cliente: 'José Lema',
};

export const REPORTE: Reporte = {
  clienteId: 'jlema',
  cliente: 'José Lema',
  fechaInicio: '2026-10-01',
  fechaFin: '2026-10-31',
  fechaGeneracion: '2026-10-31T12:00:00',
  totalCreditos: 100,
  totalDebitos: 575,
  cuentas: [
    {
      numeroCuenta: '478758',
      tipoCuenta: 'AHORRO',
      saldoInicial: 2000,
      estado: true,
      saldoDisponible: 1525,
      totalCreditos: 100,
      totalDebitos: 575,
      movimientos: [
        {
          fecha: '2026-10-01T10:16:00',
          cliente: 'José Lema',
          numeroCuenta: '478758',
          tipo: 'AHORRO',
          saldoInicial: 2000,
          estado: true,
          tipoMovimiento: 'DEBITO',
          movimiento: -575,
          saldoDisponible: 1425,
        },
        {
          fecha: '2026-10-02T09:00:00',
          cliente: 'José Lema',
          numeroCuenta: '478758',
          tipo: 'AHORRO',
          saldoInicial: 1425,
          estado: true,
          tipoMovimiento: 'CREDITO',
          movimiento: 100,
          saldoDisponible: 1525,
        },
      ],
    },
    {
      numeroCuenta: '585545',
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 1000,
      estado: true,
      saldoDisponible: 1000,
      totalCreditos: 0,
      totalDebitos: 0,
      movimientos: [],
    },
  ],
};

/** Cuerpo de error con la forma uniforme del backend. */
export function respuestaError(
  estado: number,
  mensaje: string,
  errores?: { campo: string; mensaje: string }[],
) {
  return {
    fecha: '2026-10-01T10:00:00',
    estado,
    error: 'Error',
    mensaje,
    ruta: '/api',
    ...(errores ? { errores } : {}),
  };
}
