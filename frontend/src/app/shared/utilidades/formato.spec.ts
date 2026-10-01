import {
  claseEstado,
  claseValor,
  formatearFecha,
  formatearFechaHora,
  formatearMoneda,
  nombreGenero,
  nombreTipoCuenta,
  nombreTipoMovimiento,
  textoEstado,
} from './formato';

describe('formato', () => {
  it('muestra los montos con separador de miles y dos decimales, como el PDF', () => {
    expect(formatearMoneda(1425)).toBe('1,425.00');
    expect(formatearMoneda(-575)).toBe('-575.00');
    expect(formatearMoneda(0)).toBe('0.00');
  });

  it('formatea fechas ISO del backend en dia, mes y anio', () => {
    expect(formatearFechaHora('2026-10-01T09:05:30.123')).toBe('01/10/2026 09:05');
    expect(formatearFecha('2026-10-31')).toBe('31/10/2026');
  });

  it('traduce los enums a texto legible', () => {
    expect(nombreGenero('FEMENINO')).toBe('Femenino');
    expect(nombreTipoCuenta('CORRIENTE')).toBe('Corriente');
    expect(nombreTipoMovimiento('CREDITO')).toBe('Crédito');
    expect(nombreTipoMovimiento('DEBITO')).toBe('Débito');
  });

  it('marca el estado y el signo del valor con clases distintas', () => {
    expect(textoEstado(true)).toBe('Activo');
    expect(textoEstado(false)).toBe('Inactivo');
    expect(claseEstado(true)).toContain('insignia--activa');
    expect(claseEstado(false)).toContain('insignia--inactiva');
    expect(claseValor(-1)).toContain('valor--debito');
    expect(claseValor(0)).toContain('valor--credito');
  });
});
