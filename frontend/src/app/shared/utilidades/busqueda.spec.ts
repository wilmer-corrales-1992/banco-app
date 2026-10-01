import { filtrarPorTexto, normalizarTexto } from './busqueda';

describe('busqueda rapida', () => {
  const clientes = [
    { nombre: 'José Lema', clienteId: 'jlema' },
    { nombre: 'Marianela Montalvo', clienteId: 'mmontalvo' },
  ];
  const valores = (c: { nombre: string; clienteId: string }) => [c.nombre, c.clienteId];

  it('normaliza mayusculas y tildes', () => {
    expect(normalizarTexto('  JOSÉ ')).toBe('jose');
  });

  it('encuentra sin importar tildes ni mayusculas', () => {
    expect(filtrarPorTexto(clientes, 'jose', valores)).toEqual([clientes[0]]);
  });

  it('exige todas las palabras en cualquier orden', () => {
    expect(filtrarPorTexto(clientes, 'montalvo marianela', valores)).toEqual([clientes[1]]);
    expect(filtrarPorTexto(clientes, 'lema montalvo', valores)).toEqual([]);
  });

  it('sin texto devuelve todas las filas', () => {
    expect(filtrarPorTexto(clientes, '   ', valores)).toEqual(clientes);
  });
});
