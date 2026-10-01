import { TestBed } from '@angular/core/testing';
import { DURACION_NOTIFICACION_MS, Notificaciones } from './notificaciones';

describe('Notificaciones', () => {
  let notificaciones: Notificaciones;

  beforeEach(() => {
    jest.useFakeTimers();
    notificaciones = TestBed.inject(Notificaciones);
  });

  afterEach(() => jest.useRealTimers());

  it('agrega avisos de exito y de error en orden', () => {
    notificaciones.exito('Cliente creado');
    notificaciones.error('Saldo no disponible');

    expect(notificaciones.activas().map((n) => [n.tipo, n.mensaje])).toEqual([
      ['exito', 'Cliente creado'],
      ['error', 'Saldo no disponible'],
    ]);
  });

  it('cada aviso desaparece solo despues de su duracion', () => {
    notificaciones.exito('Primero');
    jest.advanceTimersByTime(DURACION_NOTIFICACION_MS / 2);
    notificaciones.exito('Segundo');

    jest.advanceTimersByTime(DURACION_NOTIFICACION_MS / 2);
    expect(notificaciones.activas().map((n) => n.mensaje)).toEqual(['Segundo']);

    jest.advanceTimersByTime(DURACION_NOTIFICACION_MS / 2);
    expect(notificaciones.activas()).toEqual([]);
  });

  it('se puede cerrar un aviso antes de tiempo', () => {
    notificaciones.error('Error');
    const [aviso] = notificaciones.activas();

    notificaciones.cerrar(aviso.id);

    expect(notificaciones.activas()).toEqual([]);
  });
});
