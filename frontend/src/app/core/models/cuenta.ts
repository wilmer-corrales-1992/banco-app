export type TipoCuenta = 'AHORRO' | 'CORRIENTE';

export const TIPOS_CUENTA: readonly TipoCuenta[] = ['AHORRO', 'CORRIENTE'];

export interface Cuenta {
  id: number;
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  clienteId: string;
  cliente: string;
}

export interface CuentaSolicitud {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  clienteId: string;
}
