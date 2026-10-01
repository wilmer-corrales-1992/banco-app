import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { CUENTA_AHORRO, CUENTA_CERO, JOSE, RETIRO, respuestaError } from '../../testing/datos';
import {
  boton,
  buscar,
  buscarTodos,
  clic,
  elegir,
  enviar,
  escribir,
  textos,
} from '../../testing/dom';
import { App } from '../app';
import { errorApiInterceptor } from '../core/http/error-api.interceptor';
import { Notificaciones } from '../core/notificaciones/notificaciones';
import { CuentaFormulario } from './cuentas/cuenta-formulario';
import { CuentasPagina } from './cuentas/cuentas-pagina';
import { MovimientosPagina } from './movimientos/movimientos-pagina';

describe('pantallas', () => {
  let servidor: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorApiInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  it('el layout tiene la marca BANCO y el menu del diseno', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    expect(buscar(fixture, '.cabecera__marca').textContent?.trim()).toBe('BANCO');
    const enlaces = buscarTodos<HTMLAnchorElement>(fixture, 'nav a');
    expect(enlaces.map((a) => a.textContent?.trim())).toEqual([
      'Clientes',
      'Cuentas',
      'Movimientos',
      'Reportes',
    ]);
    expect(enlaces.map((a) => a.getAttribute('href'))).toEqual([
      '/clientes',
      '/cuentas',
      '/movimientos',
      '/reportes',
    ]);
  });

  it('Cuentas lista con saldo formateado y estado', async () => {
    const fixture = TestBed.createComponent(CuentasPagina);
    await fixture.whenStable();
    servidor.expectOne('/api/cuentas').flush([CUENTA_AHORRO, { ...CUENTA_CERO, estado: false }]);
    await fixture.whenStable();

    expect(textos(fixture, 'tbody tr:first-child td').slice(0, 5)).toEqual([
      '478758',
      'Ahorro',
      '2,000.00',
      'Activo',
      'José Lema',
    ]);
    expect(boton(fixture, 'Activar')).toBeTruthy();
  });

  it('el formulario de cuenta valida digitos y saldo, y crea con el clienteId elegido', async () => {
    const fixture = TestBed.createComponent(CuentaFormulario);
    await fixture.whenStable();
    servidor.expectOne('/api/clientes').flush([JOSE]);
    await fixture.whenStable();

    await escribir(fixture, '#cuenta-numero', '12ab');
    await escribir(fixture, '#cuenta-saldo', '-1');
    expect(textos(fixture, '.campo__error')).toEqual([
      'Solo se admiten dígitos',
      'El saldo inicial no puede ser negativo',
    ]);

    await escribir(fixture, '#cuenta-numero', '585545');
    await elegir(fixture, '#cuenta-tipo', 'Corriente');
    await escribir(fixture, '#cuenta-saldo', '1000');
    await elegir(fixture, '#cuenta-cliente', 'José Lema (jlema)');
    await enviar(fixture);

    const peticion = servidor.expectOne({ method: 'POST', url: '/api/cuentas' });
    expect(peticion.request.body).toEqual({
      numeroCuenta: '585545',
      tipoCuenta: 'CORRIENTE',
      saldoInicial: 1000,
      estado: true,
      clienteId: 'jlema',
    });
    peticion.flush({ ...CUENTA_AHORRO, numeroCuenta: '585545' });
  });

  it('Movimientos muestra el valor con signo y color, y avisa si no se puede eliminar', async () => {
    const fixture = TestBed.createComponent(MovimientosPagina);
    await fixture.whenStable();
    servidor.expectOne('/api/movimientos').flush([RETIRO]);
    await fixture.whenStable();

    expect(textos(fixture, 'tbody tr td').slice(0, 7)).toEqual([
      '01/10/2026 10:16',
      '478758',
      'Ahorro',
      'José Lema',
      'Débito',
      '-575.00',
      '1,425.00',
    ]);
    expect(buscar(fixture, 'tbody .valor--debito').textContent).toBe('-575.00');

    await clic(fixture, buscar<HTMLButtonElement>(fixture, 'tbody .accion--peligro'));
    await clic(fixture, boton(fixture, 'Eliminar', '[role=dialog]'));
    const mensaje = 'Solo se puede modificar o eliminar el ultimo movimiento de la cuenta';
    servidor
      .expectOne('/api/movimientos/1')
      .flush(respuestaError(409, mensaje), { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(TestBed.inject(Notificaciones).activas()[0]).toEqual(
      expect.objectContaining({ tipo: 'error', mensaje }),
    );
  });
});
