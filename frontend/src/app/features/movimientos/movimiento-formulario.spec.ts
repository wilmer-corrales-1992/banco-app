import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CUENTA_AHORRO, CUENTA_CERO, RETIRO, respuestaError } from '../../../testing/datos';
import { buscar, elegir, enviar, escribir, textos } from '../../../testing/dom';
import { errorApiInterceptor } from '../../core/http/error-api.interceptor';
import { Movimiento } from '../../core/models/movimiento';
import { MovimientoFormulario } from './movimiento-formulario';

describe('MovimientoFormulario', () => {
  let servidor: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([errorApiInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  async function montar(movimiento: Movimiento | null = null) {
    const fixture = TestBed.createComponent(MovimientoFormulario);
    fixture.componentRef.setInput('movimiento', movimiento);
    const guardado = jest.fn();
    fixture.componentInstance.guardado.subscribe(guardado);
    await fixture.whenStable();
    servidor.expectOne('/api/cuentas').flush([CUENTA_AHORRO, CUENTA_CERO]);
    await fixture.whenStable();
    return { fixture, guardado };
  }

  it('ofrece las cuentas con numero, tipo y cliente', async () => {
    const { fixture } = await montar();

    expect(textos(fixture, '#movimiento-cuenta option')).toEqual([
      'Seleccione',
      '478758 Ahorro (José Lema)',
      '496825 Ahorro (Marianela Montalvo)',
    ]);
  });

  it.each([
    ['0', 'El valor debe ser mayor que cero'],
    ['10.555', 'Admite hasta 2 decimales'],
  ])('rechaza el valor %s', async (valor, mensaje) => {
    const { fixture } = await montar();

    await escribir(fixture, '#movimiento-valor', valor);

    expect(textos(fixture, '.campo__error')).toEqual([mensaje]);
  });

  it('registra el retiro con el valor en positivo y el tipo DEBITO', async () => {
    const { fixture, guardado } = await montar();
    await elegir(fixture, '#movimiento-cuenta', '478758 Ahorro (José Lema)');
    await elegir(fixture, '#movimiento-tipo', 'Débito');
    await escribir(fixture, '#movimiento-valor', '575');

    await enviar(fixture);
    const peticion = servidor.expectOne({ method: 'POST', url: '/api/movimientos' });
    expect(peticion.request.body).toEqual({
      numeroCuenta: '478758',
      tipoMovimiento: 'DEBITO',
      valor: 575,
    });
    peticion.flush(RETIRO);
    await fixture.whenStable();

    expect(guardado).toHaveBeenCalledWith(RETIRO);
  });

  it.each(['Saldo no disponible', 'Cupo diario excedido'])(
    'muestra el mensaje exacto "%s" que devuelve el backend',
    async (mensaje) => {
      const { fixture, guardado } = await montar();
      await elegir(fixture, '#movimiento-cuenta', '496825 Ahorro (Marianela Montalvo)');
      await elegir(fixture, '#movimiento-tipo', 'Débito');
      await escribir(fixture, '#movimiento-valor', '10');

      await enviar(fixture);
      servidor
        .expectOne('/api/movimientos')
        .flush(respuestaError(422, mensaje), { status: 422, statusText: 'Unprocessable Entity' });
      await fixture.whenStable();

      expect(buscar(fixture, '.alerta--error').textContent?.trim()).toBe(mensaje);
      expect(guardado).not.toHaveBeenCalled();
    },
  );

  it('al editar bloquea la cuenta, muestra el valor positivo y usa PUT', async () => {
    const { fixture } = await montar(RETIRO);

    expect(buscar<HTMLSelectElement>(fixture, '#movimiento-cuenta').disabled).toBe(true);
    expect(buscar<HTMLInputElement>(fixture, '#movimiento-valor').value).toBe('575');

    await escribir(fixture, '#movimiento-valor', '600');
    await enviar(fixture);

    const peticion = servidor.expectOne({ method: 'PUT', url: '/api/movimientos/1' });
    expect(peticion.request.body).toEqual({
      numeroCuenta: '478758',
      tipoMovimiento: 'DEBITO',
      valor: 600,
    });
    peticion.flush({ ...RETIRO, valor: -600, saldo: 1400 });
  });
});
