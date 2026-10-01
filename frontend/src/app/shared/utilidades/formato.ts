import { formatDate, formatNumber } from '@angular/common';
import { Genero } from '../../core/models/cliente';
import { TipoCuenta } from '../../core/models/cuenta';
import { TipoMovimiento } from '../../core/models/movimiento';

const LOCALE = 'en-US';

const NOMBRES_GENERO: Record<Genero, string> = {
  MASCULINO: 'Masculino',
  FEMENINO: 'Femenino',
  OTRO: 'Otro',
};

const NOMBRES_TIPO_CUENTA: Record<TipoCuenta, string> = {
  AHORRO: 'Ahorro',
  CORRIENTE: 'Corriente',
};

const NOMBRES_TIPO_MOVIMIENTO: Record<TipoMovimiento, string> = {
  CREDITO: 'Crédito',
  DEBITO: 'Débito',
};

/** 1425 se muestra como 1,425.00, igual que en el PDF del reporte. */
export function formatearMoneda(valor: number): string {
  return formatNumber(valor, LOCALE, '1.2-2');
}

export function formatearFechaHora(iso: string): string {
  return formatDate(iso, 'dd/MM/yyyy HH:mm', LOCALE);
}

export function formatearFecha(iso: string): string {
  return formatDate(iso, 'dd/MM/yyyy', LOCALE);
}

export function nombreGenero(genero: Genero): string {
  return NOMBRES_GENERO[genero];
}

export function nombreTipoCuenta(tipo: TipoCuenta): string {
  return NOMBRES_TIPO_CUENTA[tipo];
}

export function nombreTipoMovimiento(tipo: TipoMovimiento): string {
  return NOMBRES_TIPO_MOVIMIENTO[tipo];
}

export function textoEstado(activo: boolean): string {
  return activo ? 'Activo' : 'Inactivo';
}

export function claseEstado(activo: boolean): string {
  return activo ? 'insignia insignia--activa' : 'insignia insignia--inactiva';
}

export function claseValor(valor: number): string {
  return valor < 0 ? 'valor valor--debito' : 'valor valor--credito';
}
