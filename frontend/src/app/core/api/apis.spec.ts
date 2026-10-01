import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { JOSE, REPORTE } from '../../../testing/datos';
import { ClienteApi } from './cliente-api';
import { CuentaApi } from './cuenta-api';
import { MovimientoApi } from './movimiento-api';
import { API_URL } from './recurso-api';
import { ReporteApi } from './reporte-api';

describe('servicios de la API', () => {
  let servidor: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servidor = TestBed.inject(HttpTestingController);
  });

  afterEach(() => servidor.verify());

  describe('RecursoApi (a traves de ClienteApi)', () => {
    let api: ClienteApi;

    beforeEach(() => (api = TestBed.inject(ClienteApi)));

    it('lista con GET /api/clientes', async () => {
      const respuesta = firstValueFrom(api.listar());
      const peticion = servidor.expectOne('/api/clientes');
      expect(peticion.request.method).toBe('GET');
      peticion.flush([JOSE]);
      await expect(respuesta).resolves.toEqual([JOSE]);
    });

    it('obtiene, crea, reemplaza, modifica y elimina con el verbo y la ruta correctos', () => {
      const casos = [
        { llamar: () => api.obtener(1), metodo: 'GET', url: '/api/clientes/1', cuerpo: null },
        {
          llamar: () => api.crear({ ...JOSE, contrasena: '1234' }),
          metodo: 'POST',
          url: '/api/clientes',
          cuerpo: 'contrasena',
        },
        {
          llamar: () => api.actualizar(1, { ...JOSE, contrasena: null }),
          metodo: 'PUT',
          url: '/api/clientes/1',
          cuerpo: 'nombre',
        },
        {
          llamar: () => api.actualizarParcial(1, { estado: false }),
          metodo: 'PATCH',
          url: '/api/clientes/1',
          cuerpo: 'estado',
        },
        { llamar: () => api.eliminar(1), metodo: 'DELETE', url: '/api/clientes/1', cuerpo: null },
      ];

      casos.forEach(({ llamar, metodo, url, cuerpo }) => {
        llamar().subscribe();
        const peticion = servidor.expectOne({ method: metodo, url });
        if (cuerpo) {
          expect(peticion.request.body).toHaveProperty(cuerpo);
        }
        peticion.flush(null);
      });
    });
  });

  it('cada recurso usa su propia ruta', () => {
    TestBed.inject(CuentaApi).listar({ clienteId: 'jlema' }).subscribe();
    TestBed.inject(MovimientoApi).listar().subscribe();

    servidor.expectOne('/api/cuentas?clienteId=jlema').flush([]);
    servidor.expectOne('/api/movimientos').flush([]);
  });

  it('respeta una ruta base distinta configurada con API_URL', () => {
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: 'http://api.banco' },
      ],
    });
    const otroServidor = TestBed.inject(HttpTestingController);

    TestBed.inject(ClienteApi).listar().subscribe();

    otroServidor.expectOne('http://api.banco/clientes').flush([]);
    otroServidor.verify();
  });

  describe('ReporteApi', () => {
    const filtro = { clienteId: 'jlema', fechaInicio: '2026-10-01', fechaFin: '2026-10-31' };

    it('pide el JSON con las fechas separadas por coma', async () => {
      const respuesta = firstValueFrom(TestBed.inject(ReporteApi).generar(filtro));
      const peticion = servidor.expectOne((p) => p.url === '/api/reportes');

      expect(peticion.request.params.get('fecha')).toBe('2026-10-01,2026-10-31');
      expect(peticion.request.params.get('clienteId')).toBe('jlema');
      expect(peticion.request.params.get('formato')).toBe('json');
      peticion.flush(REPORTE);
      await expect(respuesta).resolves.toEqual(REPORTE);
    });

    it('pide el PDF con formato=pdf', () => {
      TestBed.inject(ReporteApi).generarPdf(filtro).subscribe();
      const peticion = servidor.expectOne((p) => p.url === '/api/reportes');
      expect(peticion.request.params.get('formato')).toBe('pdf');
      peticion.flush({
        nombreArchivo: 'a.pdf',
        tipoContenido: 'application/pdf',
        contenidoBase64: 'JVBERi0=',
      });
    });
  });
});
