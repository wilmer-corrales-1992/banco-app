import { TipoCuenta } from './cuenta';
import { TipoMovimiento } from './movimiento';

export interface ReporteMovimiento {
  fecha: string;
  cliente: string;
  numeroCuenta: string;
  tipo: TipoCuenta;
  /** Saldo de la cuenta justo antes del movimiento. */
  saldoInicial: number;
  estado: boolean;
  tipoMovimiento: TipoMovimiento;
  movimiento: number;
  saldoDisponible: number;
}

export interface ReporteCuenta {
  numeroCuenta: string;
  tipoCuenta: TipoCuenta;
  saldoInicial: number;
  estado: boolean;
  saldoDisponible: number;
  totalCreditos: number;
  totalDebitos: number;
  movimientos: ReporteMovimiento[];
}

export interface Reporte {
  clienteId: string;
  cliente: string;
  fechaInicio: string;
  fechaFin: string;
  fechaGeneracion: string;
  totalCreditos: number;
  totalDebitos: number;
  cuentas: ReporteCuenta[];
}

export interface ReportePdf {
  nombreArchivo: string;
  tipoContenido: string;
  contenidoBase64: string;
}
