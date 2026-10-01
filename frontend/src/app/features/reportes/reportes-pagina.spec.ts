import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { JOSE, MARIANELA, REPORTE, respuestaError } from '../../../testing/datos';
import { boton, buscar, clic, elegir, enviar, escribir, textos } from '../../../testing/dom';
import { errorApiInterceptor } from '../../core/http/error-api.interceptor';
import { Notificaciones } from '../../core/notificaciones/notificaciones';
import { fechaIso, primerDiaDelMes } from '../../shared/utilidades/fechas';
import { ReportesPagina } from './reportes-pagina';

describe('ReportesPagina', () => {
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

  async function montar() {
    const fixture = TestBed.createComponent(ReportesPagina);
    await fixture.whenStable();
    servidor.expectOne('/api/clientes').flush([JOSE, MARIANELA]);
    await fixture.whenStable();
    return fixture;
  }

  async function generarParaJose() {
    const fixture = await montar();
    await elegir(fixture, '#reporte-cliente', 'José Lema (jlema)');
    await escribir(fixture, '#reporte-inicio', '2026-10-01');
    await escribir(fixture, '#reporte-fin', '2026-10-31');
    await enviar(fixture);
    return fixture;
  }

  it('propone por defecto el mes en curso', async () => {
    const fixture = await montar();
    const hoy = new Date();

    expect(buscar<HTMLInputElement>(fixture, '#reporte-inicio').value).toBe(
      fechaIso(primerDiaDelMes(hoy)),
    );
    expect(buscar<HTMLInputElement>(fixture, '#reporte-fin').value).toBe(fechaIso(hoy));
  });

  it('sin cliente no consulta la API y lo indica en el campo', async () => {
    const fixture = await montar();

    await enviar(fixture);

    expect(textos(fixture, '.campo__error')).toEqual(['Este campo es obligatorio']);
    servidor.expectNone((p) => p.url === '/api/reportes');
  });

  it('con fechas invertidas avisa y no consulta la API', async () => {
    const fixture = await montar();
    await elegir(fixture, '#reporte-cliente', 'José Lema (jlema)');
    await escribir(fixture, '#reporte-inicio', '2026-11-01');
    await escribir(fixture, '#reporte-fin', '2026-10-31');

    await enviar(fixture);

    expect(buscar(fixture, '.alerta--error').textContent).toContain('no puede ser posterior');
    servidor.expectNone((p) => p.url === '/api/reportes');
  });

  it('genera el estado de cuenta con resumen, una tabla por cuenta y sus totales', async () => {
    const fixture = await generarParaJose();

    const peticion = servidor.expectOne((p) => p.url === '/api/reportes');
    expect(peticion.request.params.get('fecha')).toBe('2026-10-01,2026-10-31');
    expect(peticion.request.params.get('clienteId')).toBe('jlema');
    peticion.flush(REPORTE);
    await fixture.whenStable();

    expect(textos(fixture, '.resumen dd')).toEqual(['2', '100.00', '575.00']);
    expect(textos(fixture, '.reporte__cuenta h3')).toEqual([
      'Cuenta 478758 Ahorro',
      'Cuenta 585545 Corriente',
    ]);
    const primeraTabla = buscar(fixture, '.reporte__cuenta');
    const celdas = Array.from(primeraTabla.querySelectorAll('tbody tr:first-child td')).map((td) =>
      td.textContent?.trim(),
    );
    expect(celdas).toEqual([
      '01/10/2026 10:16',
      'José Lema',
      '478758',
      'Ahorro',
      '2,000.00',
      'Activo',
      '-575.00',
      '1,425.00',
    ]);
    expect(textos(fixture, '.reporte__cuenta:last-child tbody td')).toEqual([
      'Sin movimientos en el periodo',
    ]);
  });

  it('muestra el error del backend si el reporte falla', async () => {
    const fixture = await generarParaJose();

    servidor
      .expectOne((p) => p.url === '/api/reportes')
      .flush(respuestaError(404, 'Cliente no encontrado con clienteId jlema'), {
        status: 404,
        statusText: 'Not Found',
      });
    await fixture.whenStable();

    expect(buscar(fixture, '.alerta--error').textContent?.trim()).toBe(
      'Cliente no encontrado con clienteId jlema',
    );
  });

  it('Descargar PDF pide formato pdf y guarda el archivo con el nombre que da la API', async () => {
    const crear = jest.fn(() => 'blob:pdf');
    Object.assign(URL, { createObjectURL: crear, revokeObjectURL: jest.fn() });
    const nombres: string[] = [];
    const clicEnlace = jest
      .spyOn(HTMLAnchorElement.prototype, 'click')
      .mockImplementation(function (this: HTMLAnchorElement) {
        nombres.push(this.download);
      });
    const fixture = await montar();
    await elegir(fixture, '#reporte-cliente', 'José Lema (jlema)');

    await clic(fixture, boton(fixture, 'Descargar PDF'));
    const peticion = servidor.expectOne((p) => p.url === '/api/reportes');
    expect(peticion.request.params.get('formato')).toBe('pdf');
    peticion.flush({
      nombreArchivo: 'estado-cuenta-jlema.pdf',
      tipoContenido: 'application/pdf',
      contenidoBase64: 'JVBERi0=',
    });
    await fixture.whenStable();

    expect(nombres).toEqual(['estado-cuenta-jlema.pdf']);
    expect(TestBed.inject(Notificaciones).activas()[0].mensaje).toBe(
      'Se descargó estado-cuenta-jlema.pdf',
    );
    clicEnlace.mockRestore();
  });
});
