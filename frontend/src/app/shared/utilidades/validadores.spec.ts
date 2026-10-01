import { FormControl, FormGroup } from '@angular/forms';
import { entero, maxDecimales, noVacio, rangoFechas } from './validadores';

describe('validadores', () => {
  it.each([null, undefined, '', '   '])('noVacio rechaza %p', (valor) => {
    expect(noVacio(new FormControl(valor))).toEqual({ required: true });
  });

  it('noVacio acepta texto y numeros', () => {
    expect(noVacio(new FormControl(' a '))).toBeNull();
    expect(noVacio(new FormControl(0))).toBeNull();
  });

  it('entero solo acepta numeros sin decimales', () => {
    expect(entero(new FormControl(30))).toBeNull();
    expect(entero(new FormControl(null))).toBeNull();
    expect(entero(new FormControl(30.5))).toEqual({ entero: true });
  });

  it('maxDecimales limita la cantidad de decimales', () => {
    const dosDecimales = maxDecimales(2);
    expect(dosDecimales(new FormControl(575))).toBeNull();
    expect(dosDecimales(new FormControl(575.25))).toBeNull();
    expect(dosDecimales(new FormControl(null))).toBeNull();
    expect(dosDecimales(new FormControl(1.234))).toEqual({ maxDecimales: { maximo: 2 } });
  });

  it('rangoFechas rechaza un inicio posterior al fin', () => {
    const grupo = (inicio: string, fin: string) =>
      new FormGroup({ inicio: new FormControl(inicio), fin: new FormControl(fin) });
    const validador = rangoFechas('inicio', 'fin');

    expect(validador(grupo('2026-10-01', '2026-10-31'))).toBeNull();
    expect(validador(grupo('2026-10-31', '2026-10-31'))).toBeNull();
    expect(validador(grupo('2026-11-01', '2026-10-31'))).toEqual({ rangoFechas: true });
    expect(validador(grupo('', '2026-10-31'))).toBeNull();
  });
});
