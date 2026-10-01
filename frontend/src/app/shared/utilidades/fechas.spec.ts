import { fechaIso, primerDiaDelMes } from './fechas';

describe('fechas', () => {
  it('fechaIso usa la fecha local con ceros a la izquierda', () => {
    expect(fechaIso(new Date(2026, 0, 5, 23, 59))).toBe('2026-01-05');
    expect(fechaIso(new Date(2026, 11, 31))).toBe('2026-12-31');
  });

  it('primerDiaDelMes conserva anio y mes', () => {
    expect(fechaIso(primerDiaDelMes(new Date(2026, 9, 17)))).toBe('2026-10-01');
  });
});
