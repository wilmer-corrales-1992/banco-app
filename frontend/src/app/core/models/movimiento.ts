import { TipoCuenta } from './cuenta';

export type TipoMovimiento = 'CREDITO' | 'DEBITO';

export const TIPOS_MOVIMIENTO: readonly TipoMovimiento[] = ['CREDITO', 'DEBITO'];

/** El valor llega con signo: positivo para creditos y negativo para debitos. */
export interface Movimiento {
  id: number;
  fecha: string;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  tipoMovimiento: TipoMovimiento;
  valor: number;
  saldo: number;
  cliente: string;
}

/** El valor se envia siempre positivo; el backend le pone el signo segun el tipo. */
export interface MovimientoSolicitud {
  numeroCuenta: string;
  tipoMovimiento: TipoMovimiento;
  valor: number;
}
